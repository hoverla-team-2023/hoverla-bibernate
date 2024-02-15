package com.bibernate.hoverla.session.dirtycheck;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.reflect.FieldUtils;

import com.bibernate.hoverla.exceptions.IllegalFieldAccessException;
import com.bibernate.hoverla.metamodel.EntityMapping;
import com.bibernate.hoverla.metamodel.FieldMapping;
import com.bibernate.hoverla.session.SessionImplementor;
import com.bibernate.hoverla.session.cache.EntityEntry;
import com.bibernate.hoverla.session.cache.EntityKey;
import com.bibernate.hoverla.session.cache.EntityState;
import com.bibernate.hoverla.utils.EntityProxyUtils;
import com.bibernate.hoverla.utils.EntityUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static java.util.function.Predicate.not;

@Slf4j
@RequiredArgsConstructor
public class DirtyCheckServiceImpl implements DirtyCheckService {

  private final SessionImplementor sessionImplementor;

  @Override
  public List<Object> findDirtyEntities() {
    Map<EntityKey<?>, EntityEntry> persistenceContextMap = sessionImplementor.getPersistenceContext().getEntityKeyEntityEntryMap();

    return persistenceContextMap.entrySet().stream()
      .filter(this::isManaged)
      .filter(not(this::isReadOnly))
      .filter(entry -> !EntityProxyUtils.isUnitializedProxy(entry.getValue().getEntity()))
      .filter(entry -> isDirtyEntity(entry.getKey().entityType(), entry.getValue()))
      .map(entry -> entry.getValue().getEntity())
      .toList();
  }

  @Override
  public <T> List<DirtyFieldMapping<Object>> getUpdatedFields(T entity) {
    var entityDetails = sessionImplementor.getEntityDetails(entity);
    var entityEntry = sessionImplementor.getEntityEntry(entityDetails.entityKey());

    Object unProxied = EntityProxyUtils.unProxy(entity);
    if (unProxied == null) {
      return new ArrayList<>();
    }

    Object[] oldSnapshot = entityEntry.getSnapshot();

    int i = 0;

    List<DirtyFieldMapping<Object>> dirtyFieldMappings = new ArrayList<>();

    EntityMapping entityMapping = entityDetails.entityMapping();
    for (var field : entityMapping.getFieldMappings(FieldMapping::isUpdatable)) {
      Object object = oldSnapshot[i++];
      Object fieldValue = getFieldValue(entityMapping.getEntityClass(), unProxied, field.getFieldName());
      if (object != fieldValue) {
        dirtyFieldMappings.add(DirtyFieldMapping.of(field, fieldValue));
      }
    }
    return dirtyFieldMappings;
  }

  @Override
  public Object[] getSnapshot(Class<?> entityClass, Object entity) {
    return getSnapshot(sessionImplementor.getEntityMapping(entityClass), entity);
  }

  private Object[] getSnapshot(EntityMapping entityMapping, Object entity) {
    Object unProxied = EntityProxyUtils.unProxy(entity);
    if (unProxied == null) {
      return new Object[0];
    }

    return entityMapping.getFieldMappings(FieldMapping::isUpdatable)
      .stream()
      .map(fieldMapping -> EntityUtils.getFieldValue(fieldMapping.getFieldName(), unProxied))
      .toArray();
  }

  private boolean isManaged(Map.Entry<EntityKey<?>, EntityEntry> entry) {
    return entry.getValue().getEntityState().equals(EntityState.MANAGED);
  }

  private boolean isReadOnly(Map.Entry<EntityKey<?>, EntityEntry> entry) {
    return entry.getValue().isReadOnly();
  }

  /**
   * Determines if an entity is considered "dirty" based on its snapshots.
   *
   * @param entityType  The class of the entity to check.
   * @param entityEntry The entity entry containing the snapshots to compare.
   *
   * @return {@code true} if the entity is considered dirty, {@code false} otherwise.
   */
  private boolean isDirtyEntity(Class<?> entityType, EntityEntry entityEntry) {
    var entityMapping = sessionImplementor.getEntityMapping(entityType);

    Object[] oldSnapshot = entityEntry.getSnapshot();

    Object unProxied = EntityProxyUtils.unProxy(entityEntry.getEntity());

    int i = 0;

    for (var field : entityMapping.getFieldMappings(FieldMapping::isUpdatable)) {
      Object object = oldSnapshot[i++];
      Object fieldValue = getFieldValue(entityMapping.getEntityClass(), unProxied, field.getFieldName());
      if (object != fieldValue && field.isManyToOne()) {
        return true;
      } else if (!Objects.equals(object, fieldValue)) {
        return true;
      }
    }
    return false;
  }

  private Object getFieldValue(Class<?> entityType, Object entity, String fieldName) {
    try {
      return FieldUtils.readDeclaredField(entity, fieldName, true);
    } catch (IllegalAccessException exception) {
      throw new IllegalFieldAccessException("Failed to access field: %s for entity: %s".formatted(fieldName, entityType), exception);
    }
  }

}
