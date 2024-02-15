package com.bibernate.hoverla.session;

import java.util.List;

import com.bibernate.hoverla.collection.PersistenceLazyList;
import com.bibernate.hoverla.metamodel.EntityMapping;
import com.bibernate.hoverla.metamodel.FieldMapping;
import com.bibernate.hoverla.session.cache.CollectionKey;
import com.bibernate.hoverla.session.cache.EntityKey;
import com.bibernate.hoverla.utils.EntityUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.bibernate.hoverla.utils.EntityUtils.getEntityKey;

/**
 * Maps database rows to entity objects using the provided session implementor.
 */
@Slf4j
@RequiredArgsConstructor
public class EntityRowMapper {

  private final SessionImplementor sessionImplementor;

  /**
   * Creates an entity object of the specified type from the given row data.
   *
   * @param row         The row data retrieved from the database.
   * @param entityClass The class of the entity to be created.
   * @param <T>         The type of the entity.
   *
   * @return The entity object created from the row data.
   */
  public <T> T createEntityFromRow(Object[] row, Class<T> entityClass) {
    log.debug("Creating entity of type {} from row data.", entityClass.getSimpleName());

    var entity = EntityUtils.newInstanceOf(entityClass);
    populateFields(row, entityClass, entity);

    log.debug("Creating entity of type {} from row data.", entityClass.getSimpleName());
    return entity;
  }
  /**
   * Populates the fields of the entity object with the data from the row.
   * It also handles the population of lazy collections.
   *
   * @param row         The row data retrieved from the database.
   * @param entityClass The class of the entity.
   * @param entity      The entity object to populate.
   * @param <T>         The type of the entity.
   */
  private <T> void populateFields(Object[] row, Class<T> entityClass, T entity) {
    log.debug("Populating fields for entity class: {}", entityClass.getSimpleName());

    EntityMapping entityMapping = sessionImplementor.getEntityMapping(entityClass);

    var fieldMappings = entityMapping.getFieldMappings(mapping -> !mapping.isOneToMany());

    int i = 0;
    for (var fieldMapping : fieldMappings) {
      Object field = getFieldValue(fieldMapping, row[i++]);
      EntityUtils.setFieldValue(fieldMapping.getFieldName(), entity, field);
    }

    populateLazyCollections(entityClass, entityMapping, entity);

    log.debug("Fields populated successfully for entity class: {}", entityClass.getSimpleName());
  }
  /**
   * Retrieves the field value from the column value.
   * If the column value is null, it returns null.
   * If the field is a many-to-one reference, it resolves the reference using the session implementor.
   *
   * @param fieldMapping The field mapping for the field.
   * @param columnValue  The value from the database column.
   * @return The field value.
   */
  private <T> Object getFieldValue(FieldMapping<?> fieldMapping, Object columnValue) {
    if (columnValue == null) {
      log.trace("Column value is null for field: {}", fieldMapping.getFieldName());
      return null;
    }

    if (fieldMapping.isManyToOne()) {
      log.trace("Resolving many-to-one reference for field: {}", fieldMapping.getFieldName());
      return sessionImplementor.getReference(fieldMapping.getFieldType(), columnValue);
    }

    return columnValue;
  }
  /**
   * Populates the lazy collections of the entity.
   *
   * @param entityClass  The class of the entity.
   * @param entityMapping The entity mapping for the entity.
   * @param entity       The entity object.
   * @param <T>          The type of the entity.
   */
  private <T> void populateLazyCollections(Class<T> entityClass, EntityMapping entityMapping, T entity) {
    List<FieldMapping<?>> oneToManyMappings = entityMapping.getFieldMappings(FieldMapping::isOneToMany);

    if (oneToManyMappings.isEmpty()) {
      return;
    }

    EntityKey<T> entityKey = getEntityKey(entityClass, entity, entityMapping.getPrimaryKeyMapping().getFieldName());

    log.trace("Populating lazy collections for entity: {}", entityKey);

    oneToManyMappings.forEach(oneToManyMapping -> populateOneToManyAssociation(entity, oneToManyMapping, entityKey));
  }
  /**
   * Populates a one-to-many association of the entity.
   * It creates a new PersistenceLazyList for the association and sets it as the value of the field in the entity.
   *
   * @param entity         The entity object.
   * @param oneToManyMapping The field mapping for the one-to-many association.
   * @param entityKey      The entity key for the entity.
   * @param <T>            The type of the entity.
   */
  private <T> void populateOneToManyAssociation(T entity, FieldMapping<?> oneToManyMapping, EntityKey<T> entityKey) {
    CollectionKey<T> collectionKey = new CollectionKey<>(entityKey.entityType(), entityKey.id(), oneToManyMapping.getFieldName());

    log.debug("Populating lazy collection: {}", collectionKey);

    PersistenceLazyList<?> persistenceLazyList = new PersistenceLazyList<>(collectionKey, sessionImplementor);

    sessionImplementor.getPersistenceContext().manageCollection(collectionKey, persistenceLazyList);

    EntityUtils.setFieldValue(oneToManyMapping.getFieldName(), entity, persistenceLazyList);
  }

}
