package com.bibernate.hoverla.session.dirtycheck;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.logging.log4j.core.util.ReflectionUtil;

import com.bibernate.hoverla.exceptions.IllegalFieldAccessException;
import com.bibernate.hoverla.metamodel.EntityMapping;
import com.bibernate.hoverla.metamodel.FieldMapping;
import com.bibernate.hoverla.session.SessionImplementor;
import com.bibernate.hoverla.session.cache.EntityEntry;
import com.bibernate.hoverla.session.cache.EntityKey;
import com.bibernate.hoverla.session.cache.EntityState;
import com.bibernate.hoverla.utils.EntityProxyUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toMap;

//todo Yevhenii Savonenko: rename it to DirtyCheckServiceImpl
@Slf4j
@RequiredArgsConstructor
public class EntityEntryUpdateStateVerifierImpl implements EntityEntryUpdateStateVerifier {

  private final SessionImplementor sessionImplementor;

  @Override
  public Object[] findDirtyEntities(Map<EntityKey<?>, EntityEntry> persistenceContextMap) {
    return persistenceContextMap.entrySet().stream()
      .filter(this::isManaged)
      .filter(not(this::isReadOnly))
      .filter(entry -> isDirtyEntity(entry.getKey().entityType(), entry.getValue()))
      .map(entry -> entry.getValue().getEntity())
      .toArray();
  }

  @Override
  public DirtyFieldMapping<?>[] getUpdatedFields(EntityEntry entityEntry) {
    Object entity = entityEntry.getEntity();
    var entityDetails = sessionImplementor.getEntityDetails(entity);

    Map<String, Object> oldSnapshot = entityEntry.getSnapshot();
    Map<String, Object> currentSnapshot = getSnapshot(entityDetails.entityMapping(), entityEntry.getEntity());

    Map<String, FieldMapping<?>> fieldMappings = entityDetails.entityMapping().getFieldMappingMap();

    return fieldMappings.entrySet().stream()
      .filter(entry -> entry.getValue().isUpdatable())
      .filter(entry -> isDirtyField(entry.getKey(), oldSnapshot, currentSnapshot))
      .map(entry -> getDirtyFieldMapping(entityDetails.entityMapping().getEntityClass(), entity, entry))
      .toArray(DirtyFieldMapping[]::new);
  }

  private boolean isManaged(Map.Entry<EntityKey<?>, EntityEntry> entry) {
    return entry.getValue().getEntityState().equals(EntityState.MANAGED);
  }

  private boolean isReadOnly(Map.Entry<EntityKey<?>, EntityEntry> entry) {
    return entry.getValue().isReadOnly();
  }

  private boolean isDirtyEntity(Class<?> entityType, EntityEntry entityEntry) {
    var entityMapping = sessionImplementor.getEntityMapping(entityType);

    Map<String, Object> oldSnapshot = entityEntry.getSnapshot();
    Map<String, Object> currentSnapshot = getSnapshot(entityMapping, entityEntry.getEntity());

    if (oldSnapshot == null) {
      return false;
    }

    if (oldSnapshot.size() != currentSnapshot.size()) {
      return true;
    }

    for (Map.Entry<String, Object> entry : oldSnapshot.entrySet()) {
      String fieldName = entry.getKey();
      if (notEqual(entry.getValue(), currentSnapshot.get(fieldName))) {
        return true;
      }
    }

    return false;
  }

  private boolean isDirtyField(String field, Map<String, Object> oldSnapshot, Map<String, Object> currentSnapshot) {
    Object oldValue = oldSnapshot.get(field);
    Object newValue = currentSnapshot.get(field);

    return notEqual(oldValue, newValue);
  }

  @SuppressWarnings("unchecked")
  private <T> DirtyFieldMapping<T> getDirtyFieldMapping(Class<?> entityType, Object entity, Map.Entry<String, FieldMapping<?>> entry) {
    String fieldName = entry.getKey();
    FieldMapping<T> fieldMapping = (FieldMapping<T>) entry.getValue();
    T fieldValue = (T) getFieldValue(entityType, entity, fieldName);

    return new DirtyFieldMapping<>(fieldMapping, fieldValue);
  }

  private boolean notEqual(Object o1, Object o2) {
    if (Objects.hashCode(o1) != Objects.hashCode(o2)) {
      return true;
    }

    return !Objects.equals(o1, o2);
  }

  private Object getFieldValue(Class<?> entityType, Object entity, String fieldName) {
    try {
      return FieldUtils.readDeclaredField(entity, fieldName, true);
    } catch (IllegalAccessException exception) {
      throw new IllegalFieldAccessException("Failed to access field: %s for entity: %s".formatted(fieldName, entityType), exception);
    }
  }

  /**
   * Converts the given <code>entity</code> into a map of field name and field value. Only {@link FieldMapping#isUpdatable() updatable} fields are included
   * in the result map.
   *
   * @param entityMapping entity mapping
   * @param entity        entity to convert
   *
   * @return map with field names and their values
   */
  //TODO return Object[]
  public Map<String, Object> getSnapshot(EntityMapping entityMapping, Object entity) {
    Object object = EntityProxyUtils.unProxy(entity);
    if (object == null) {
      return null;
    }

    Class<?> entityType = entityMapping.getEntityClass();

    return Arrays.stream(entityType.getDeclaredFields())
      .filter(field -> isUpdatableField(entityMapping, field))
      .collect(toMap(Field::getName, field -> ReflectionUtil.getFieldValue(field, object)));
  }

  private boolean isUpdatableField(EntityMapping entityMapping, Field field) {
    return Optional.ofNullable(entityMapping.getFieldMapping(field.getName()))
      .map(FieldMapping::isUpdatable)
      .orElse(false);
  }

}
