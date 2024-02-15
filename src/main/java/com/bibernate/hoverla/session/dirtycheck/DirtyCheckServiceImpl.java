package com.bibernate.hoverla.session.dirtycheck;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

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
import com.bibernate.hoverla.utils.proxy.BibernateByteBuddyProxyInterceptor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static java.util.function.Predicate.not;

import static org.apache.commons.lang3.ObjectUtils.allNull;
import static org.apache.commons.lang3.ObjectUtils.anyNull;
/**
 * Implementation of the {@link DirtyCheckService} interface. This class is responsible for
 * detecting and managing dirty entities and their fields. It provides methods to find dirty
 * entities and to get the updated fields of a given entity.
 */
@Slf4j
@RequiredArgsConstructor
public class DirtyCheckServiceImpl implements DirtyCheckService {

  private final SessionImplementor sessionImplementor;
  /**
   * Finds all dirty entities in the persistence context. A dirty entity is an entity that
   * is managed, not read-only, and has changes compared to its snapshot.
   *
   * @return A list of dirty entities.
   */
  @Override
  public List<Object> findDirtyEntities() {
    Map<EntityKey<?>, EntityEntry> persistenceContextMap = sessionImplementor.getPersistenceContext().getEntityKeyEntityEntryMap();

    return persistenceContextMap.entrySet().stream()
      .filter(this::isManaged)
      .filter(not(this::isReadOnly))
      .filter(entry -> isDirtyEntity(entry.getKey().entityType(), entry.getValue()))
      .map(entry -> entry.getValue().getEntity())
      .toList();
  }
  /**
   * Retrieves the updated fields of a given entity. An updated field is a field that has
   * a different value compared to its snapshot.
   *
   * @param <T>    The type of the entity.
   * @param entity The entity to check for updated fields.
   * @return A list of dirty field mappings representing the updated fields of the entity.
   */
  @Override
  public <T> List<DirtyFieldMapping<Object>> getUpdatedFields(T entity) {
    var entityDetails = sessionImplementor.getEntityDetails(entity);
    var entityEntry = sessionImplementor.getEntityEntry(entityDetails.entityKey());

    Object unProxied = EntityProxyUtils.unProxy(entity);
    if (unProxied == null) {
      return new ArrayList<>();
    }

    Object[] oldSnapshot = entityEntry.getSnapshot();
    Object[] currentSnapshot = getSnapshot(entityDetails.entityMapping(), unProxied);

    Map<String, FieldMapping<?>> fieldMappings = entityDetails.entityMapping().getFieldNameMappingMap();
    Iterator<FieldMapping<?>> iterator = fieldMappings.values().iterator();

    List<DirtyFieldMapping<Object>> dirtyFieldMappings = new ArrayList<>();
    int index = 0;

    while (iterator.hasNext()) {
      FieldMapping<?> fieldMapping = iterator.next();
      if (fieldMapping.isUpdatable()) {
        if (valuesDiffer(oldSnapshot[index], currentSnapshot[index])) {
          dirtyFieldMappings.add(getDirtyFieldMapping(entityDetails.entityMapping().getEntityClass(), unProxied, fieldMapping));
        }
        index++;
      }
    }
    return dirtyFieldMappings;
  }
  /**
   * Creates a snapshot of the entity's field values based on the entity mapping.
   *
   * @param entityMapping The entity mapping to use for field extraction.
   * @param entity        The entity to create a snapshot for.
   * @return An array of field values representing the snapshot of the entity.
   */
  public Object[] getSnapshot(EntityMapping entityMapping, Object entity) {
    Object unProxied = EntityProxyUtils.unProxy(entity);
    if (unProxied == null) {
      return new Object[0];
    }

    return entityMapping.getFieldNameMappingMap().values().stream()
      .filter(FieldMapping::isUpdatable)
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
    Object[] currentSnapshot = getSnapshot(entityMapping, entityEntry.getEntity());

    return snapshotsDiffer(oldSnapshot, currentSnapshot);
  }

  private boolean snapshotsDiffer(Object[] oldSnapshot, Object[] currentSnapshot) {
    if (allNull(oldSnapshot, currentSnapshot)) {
      return false;
    }

    if (anyNull(oldSnapshot, currentSnapshot) || oldSnapshot.length != currentSnapshot.length) {
      return true;
    }

    for (int i = 0; i < oldSnapshot.length; i++) {
      if (valuesDiffer(oldSnapshot[i], currentSnapshot[i])) {
        return true;
      }
    }

    return false;
  }

  /**
   * This method retrieves the dirty field mapping for a given entity.
   *
   * @param entityType   the class of the entity
   * @param entity       the entity object
   * @param fieldMapping field mapping
   *
   * @return a DirtyFieldMapping object containing the field mapping and its value
   */
  @SuppressWarnings("unchecked")
  private <T> DirtyFieldMapping<T> getDirtyFieldMapping(Class<?> entityType, Object entity, FieldMapping<?> fieldMapping) {
    String fieldName = fieldMapping.getFieldName();
    FieldMapping<T> fieldMappingCasted = (FieldMapping<T>) fieldMapping;
    Object fieldValue = getFieldValue(entityType, entity, fieldName);

    if (isEntity(fieldValue)) {
      Object entityId = getEntityId(fieldValue);
      return new DirtyFieldMapping<>(fieldMappingCasted, entityId);
    }

    return new DirtyFieldMapping<>(fieldMappingCasted, fieldValue);
  }

  private boolean valuesDiffer(Object o1, Object o2) {
    // for fields that are references to entities just compare the ids
    if (isEntity(o1) && isEntity(o2)) {
      return getEntityId(o1) != getEntityId(o2);
    }

    return !Objects.equals(o1, o2);
  }

  private boolean isEntity(Object object) {
    return sessionImplementor.getPersistenceContext().getEntityKeyEntityEntryMap().values().stream()
      .anyMatch(entry -> entry.getEntity() == object);
  }

  private Object getEntityId(Object entity) {
    if (EntityProxyUtils.isProxy(entity)) {
      return Optional.ofNullable(EntityProxyUtils.getProxyInterceptor(entity))
        .map(BibernateByteBuddyProxyInterceptor::getEntityId)
        .orElse(null);
    }

    return sessionImplementor.getPersistenceContext().getEntityKeyEntityEntryMap().entrySet().stream()
      .filter(entry -> entry.getValue().getEntity() == entity)
      .map(Map.Entry::getKey)
      .map(EntityKey::id)
      .findFirst()
      .orElse(null);
  }

  private Object getFieldValue(Class<?> entityType, Object entity, String fieldName) {
    try {
      return FieldUtils.readDeclaredField(entity, fieldName, true);
    } catch (IllegalAccessException exception) {
      throw new IllegalFieldAccessException("Failed to access field: %s for entity: %s".formatted(fieldName, entityType), exception);
    }
  }

}
