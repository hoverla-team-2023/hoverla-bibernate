package com.bibernate.hoverla.session;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;

import com.bibernate.hoverla.exceptions.BibernateException;
import com.bibernate.hoverla.exceptions.OptimisticLockException;
import com.bibernate.hoverla.exceptions.PersistOperationException;
import com.bibernate.hoverla.jdbc.JdbcParameterBinding;
import com.bibernate.hoverla.jdbc.JdbcResultExtractor;
import com.bibernate.hoverla.metamodel.EntityMapping;
import com.bibernate.hoverla.metamodel.FieldMapping;
import com.bibernate.hoverla.metamodel.OneToManyMapping;
import com.bibernate.hoverla.session.cache.CollectionKey;
import com.bibernate.hoverla.session.cache.EntityKey;
import com.bibernate.hoverla.session.dirtycheck.DirtyFieldMapping;
import com.bibernate.hoverla.utils.EntityProxyUtils;
import com.bibernate.hoverla.utils.EntityUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static java.util.stream.Collectors.joining;

import static com.bibernate.hoverla.jdbc.JdbcParameterBinding.bindParameter;

/**
 * Service class for handling CRUD operations for entities.
 * <p>
 * The EntityDaoService class provides methods for inserting, updating, deleting, and loading entities
 * from the database. It works in conjunction with the underlying database session to perform these operations.
 * </p>
 */
@Slf4j
@RequiredArgsConstructor
public class EntityDaoService {

  private static final String FOR_SHARE = "FOR SHARE";
  private static final String FOR_UPDATE = "FOR UPDATE";
  private static final String NONE = "";

  private static final String DELETE_FROM_TABLE_BY_ID = "DELETE FROM %s WHERE %s = ?;";
  private static final String SELECT_FROM_TABLE_BY_COLUMN = "SELECT %s FROM %s WHERE %s = ? %s;";
  private static final String INSERT_INTO_TABLE = "INSERT INTO %s (%s) VALUES (%s);";
  private static final String UPDATE_TABLE_BY_ID = "UPDATE %s SET %s WHERE %s = ?;";
  private static final String UPDATE_TABLE_WITH_OPTIMISTIC_LOCK = "UPDATE %s SET %s, %s = ? WHERE %s = ? AND %s = ?;";

  private final SessionImplementor session;

  /**
   * Inserts a new entity into the database.
   * <p>
   * This method inserts a new entity into the database. If the entity has an identity-generated primary key,
   * the generated key is retrieved and set on the entity after insertion.
   * </p>
   *
   * @param entity the entity to insert into the database.
   * @param <T>    the type of the entity.
   */
  public <T> void insert(T entity) {
    EntityMapping entityMapping = session.getEntityMapping(entity.getClass());

    entityMapping.getFieldMappingWithOptimisticLock()
      .ifPresent(optimisticLock -> initOptimisticLock(entity, optimisticLock));

    List<FieldMapping<?>> insertableFields = entityMapping.getFieldMappings(FieldMapping::isInsertable);
    JdbcParameterBinding<?>[] parameterBindings = getInsertParameterBinding(entity, insertableFields);
    FieldMapping<?> primaryKeyMapping = entityMapping.getPrimaryKeyMapping();

    String insertStatement = INSERT_INTO_TABLE.formatted(
      entityMapping.getTableName(),
      getColumnNames(insertableFields),
      generatePlaceholders(insertableFields)
    );

    if (isIdentityGenerated(primaryKeyMapping)) {
      Object generatedKey = session.getJdbcExecutor()
        .executeUpdateAndReturnGeneratedKeys(insertStatement, parameterBindings, primaryKeyMapping.getJdbcType());

      EntityUtils.setFieldValue(primaryKeyMapping.getFieldName(), entity, generatedKey);
      return;
    }

    if (isDetached(entity)) {
      log.debug("Entity with class {} is detached. Skipping insert operation.", entityMapping.getClass());
      return;
    }

    int updatedRows = session.getJdbcExecutor().executeUpdate(insertStatement, parameterBindings);
    verifyInsertOperation(updatedRows);
  }

  /**
   * Loads an entity from the database based on the given entity key.
   * <p>
   * This method loads an entity from the database based on the given entity key, which consists of the entity's type and ID.
   * If multiple entities are found for the given entity key, an exception is thrown.
   * </p>
   *
   * @param <T>       the type of the entity.
   * @param entityKey the entity key representing the entity to load.
   *
   * @return the loaded entity, or null if no entity is found.
   */
  public <T> T load(EntityKey<T> entityKey, LockMode lockMode) {

    EntityMapping entityMapping = session.getEntityMapping(entityKey.entityType());

    FieldMapping<?> primaryKeyMapping = entityMapping.getPrimaryKeyMapping();

    String selectStatement = SELECT_FROM_TABLE_BY_COLUMN.formatted(entityMapping.getColumnNames(),
                                                                   entityMapping.getTableName(),
                                                                   primaryKeyMapping.getColumnName(),
                                                                   StringUtils.prependIfMissing(getLockModeSqlAppend(lockMode), " ")
    );

    List<T> results = session.getJdbcExecutor()
      .executeSelectQuery(selectStatement,
                          getJdbcParameterBindings(entityKey, primaryKeyMapping),
                          entityMapping.getJdbcTypes().toArray(new JdbcResultExtractor<?>[0]))
      .stream()
      .map(row -> getEntityFromRow(entityKey, row))
      .toList();

    if (results.size() > 1) {
      String errorMessage = "Multiple entities found for the given entity key : %s. Expected only one result.".formatted(entityKey);
      throw new BibernateException(errorMessage);
    }

    return results.stream().findFirst().orElse(null);
  }

  /**
   * Loads a collection of entities associated with the given collection key.
   * <p>
   * This method loads a collection of entities associated with the given collection key from the database.
   * It retrieves the entities based on the collection key's entity type and collection name.
   * </p>
   *
   * @param collectionKey the collection key representing the collection of entities to load.
   * @param <T>           the type of the entities in the collection.
   *
   * @return a list of entities loaded from the database.
   */
  public <T> List<T> loadCollection(CollectionKey<?> collectionKey) {
    EntityMapping entityMappingOfParent = session.getEntityMapping(collectionKey.entityType());
    FieldMapping<?> fieldMapping = entityMappingOfParent.getFieldMapping(collectionKey.collectionName());
    OneToManyMapping oneToManyMapping = fieldMapping.getOneToManyMapping();

    Class<T> entityType = (Class<T>) oneToManyMapping.getCollectionType();
    EntityMapping entityMapping = session.getEntityMapping(entityType);
    FieldMapping<?> joinColumn = entityMapping.getFieldMapping(oneToManyMapping.getMappedBy());

    String selectStatement = SELECT_FROM_TABLE_BY_COLUMN.formatted(entityMapping.getColumnNames(),
                                                                   entityMapping.getTableName(),
                                                                   joinColumn.getColumnName(),
                                                                   Strings.EMPTY);

    JdbcParameterBinding<?>[] bindValues = { bindParameter(collectionKey.id(),
                                                           joinColumn.getJdbcType()) };

    var jdbcResultExtractors = entityMapping.getJdbcTypes();

    List<Object[]> rows = session.getJdbcExecutor().executeSelectQuery(selectStatement,
                                                                       bindValues,
                                                                       jdbcResultExtractors.toArray(new JdbcResultExtractor<?>[0]));
    log.debug("Creating collection {}", entityType);

    return rows.stream()
      .map(row -> session.getEntityRowMapper()
        .createEntityFromRow(row, entityType))
      .collect(Collectors.toList());
  }

  /**
   * Updates the provided entity in the database.
   * <p>
   * This method updates the entity in the database if it exists. It first checks if the entity
   * is detached from the current session. If the entity is detached, the update operation is skipped.
   * If the entity is attached, it identifies the fields that have been modified since the entity was
   * loaded into the session and constructs an update query to reflect those changes in the database.
   * If the entity has an optimistic lock defined, it uses the optimistic locking strategy to prevent
   * concurrent updates to the same entity.
   *
   * @param entity the entity object to be updated.
   * @param <T>    the type of the entity.
   */
  public <T> void update(T entity) {
    var entityDetails = session.getEntityDetails(entity);
    var entityKey = entityDetails.entityKey();

    if (isDetached(entityKey)) {
      log.debug("Entity with key {} is detached. Skipping update operation.", entityKey);
      return;
    }

    log.debug("Updating entity: {}", entityKey);

    List<DirtyFieldMapping<Object>> dirtyFields = session.getDirtyCheckService().getUpdatedFields(entity);

    var entityMapping = entityDetails.entityMapping();
    FieldMapping<?> primaryKeyMapping = entityMapping.getPrimaryKeyMapping();

    String tableName = entityMapping.getTableName();
    String columnsToUpdate = getColumnsToUpdate(dirtyFields);

    entityMapping.getFieldMappingWithOptimisticLock()
      .ifPresentOrElse(
        optimisticLock -> updateEntityWithOptimisticLock(entity, tableName, columnsToUpdate, entityKey, primaryKeyMapping, dirtyFields, optimisticLock),
        () -> updateEntity(tableName, columnsToUpdate, entityKey, primaryKeyMapping, dirtyFields));
  }

  /**
   * Deletes an existing entity from the database.
   * <p>
   * This method deletes an existing entity from the database based on its primary key.
   * </p>
   *
   * @param entity the entity to delete from the database.
   * @param <T>    the type of the entity.
   */
  public <T> void delete(T entity) {
    EntityDetails<T> entityDetails = session.getEntityDetails(entity);

    if (isDetached(entityDetails.entityKey())) {
      log.debug("Entity with key {} is detached. Skipping delete operation.", entityDetails.entityKey());
      return;
    }

    log.debug("Deleting entity: {}", entityDetails.entityKey());

    FieldMapping<?> primaryKeyMapping = entityDetails.entityMapping().getPrimaryKeyMapping();
    String deleteStatement = DELETE_FROM_TABLE_BY_ID.formatted(entityDetails.entityMapping().getTableName(),
                                                               primaryKeyMapping.getColumnName());

    JdbcParameterBinding<?>[] bindValues = { bindParameter(entityDetails.entityKey().id(),
                                                           primaryKeyMapping.getJdbcType()) };

    int updatedRows = session.getJdbcExecutor().executeUpdate(deleteStatement, bindValues);

    log.debug("Entity with id {} deleted from table {}, updated rows {}", entityDetails.entityKey(), entityDetails.entityMapping().getTableName(),
              updatedRows);

    if (updatedRows == 0) {
      throw new BibernateException("Row was deleted by another transaction " + entityDetails.entityKey());
    }

  }

  private void verifyInsertOperation(int updatedRows) {
    if (updatedRows == 0) {
      throw new PersistOperationException("Row was not persisted");
    }
  }

  private <T> T getEntityFromRow(EntityKey<T> entityKey, Object[] row) {
    return session.getEntityRowMapper().createEntityFromRow(row, entityKey.entityType());
  }

  private <T> JdbcParameterBinding<?>[] getJdbcParameterBindings(EntityKey<T> entityKey, FieldMapping<?> primaryKeyMapping) {
    return new JdbcParameterBinding<?>[] { bindParameter(entityKey.id(),
                                                         primaryKeyMapping.getJdbcType()) };
  }

  private <T> void updateEntityWithOptimisticLock(T entity,
                                                  String tableName,
                                                  String columnsToUpdate,
                                                  EntityKey<?> entityKey,
                                                  FieldMapping<?> primaryKey,
                                                  List<DirtyFieldMapping<Object>> dirtyFields,
                                                  FieldMapping<?> optimistiLockFieldMapping) {
    String optimisticLockColumn = optimistiLockFieldMapping.getColumnName();

    String updateStatement = UPDATE_TABLE_WITH_OPTIMISTIC_LOCK.formatted(
      tableName,
      columnsToUpdate,
      optimisticLockColumn,
      primaryKey.getColumnName(),
      optimisticLockColumn
    );

    T unProxied = EntityProxyUtils.unProxy(entity);
    Number optimisticLockPrevValue = (Number) EntityUtils.getFieldValue(optimistiLockFieldMapping.getFieldName(), unProxied);
    Number optimisticLockNextValue = getOptimisticLockNextValue(optimisticLockPrevValue);

    List<JdbcParameterBinding<?>> parameterBindingsList = dirtyFields.stream()
      .map((DirtyFieldMapping<Object> fieldMapping) -> bindFieldParameter(fieldMapping.fieldMapping(), fieldMapping.value()))
      .collect(Collectors.toList());
    parameterBindingsList.add(bindParameter(optimisticLockNextValue, optimistiLockFieldMapping.getJdbcType()));
    parameterBindingsList.add(bindParameter(entityKey.id(), primaryKey.getJdbcType()));
    parameterBindingsList.add(bindParameter(optimisticLockPrevValue, optimistiLockFieldMapping.getJdbcType()));

    JdbcParameterBinding<?>[] parameterBindings = parameterBindingsList.toArray(JdbcParameterBinding[]::new);

    int updatedRows = session.getJdbcExecutor().executeUpdate(updateStatement, parameterBindings);

    if (updatedRows == 0) {
      throw new BibernateException("Could not update entity %s with optimistic lock value %s. Row was updated by another transaction"
                                     .formatted(entityKey, optimisticLockPrevValue));
    }

    EntityUtils.setFieldValue(optimistiLockFieldMapping.getFieldName(), unProxied, optimisticLockNextValue);

    log.debug("Entity with id {} was updated in table {}, new optimistic lock value: {}, updated rows: {}",
              entityKey, tableName, optimisticLockNextValue, updatedRows);
  }

  private JdbcParameterBinding<?> bindFieldParameter(FieldMapping<?> fieldMapping, Object value) {
    if (value == null) {
      return bindParameter(null, fieldMapping.getJdbcType());
    }
    if (fieldMapping.isManyToOne()) {
      EntityDetails<?> entityDetails = session.getEntityDetails(value);
      return bindParameter(entityDetails.entityKey().id(), fieldMapping.getJdbcType());
    }
    return bindParameter(value, fieldMapping.getJdbcType());
  }

  private void updateEntity(String tableName,
                            String columnsToUpdate,
                            EntityKey<?> entityKey,
                            FieldMapping<?> primaryKey,
                            List<DirtyFieldMapping<Object>> dirtyFields) {
    String updateStatement = UPDATE_TABLE_BY_ID.formatted(
      tableName,
      columnsToUpdate,
      primaryKey.getColumnName()
    );

    JdbcParameterBinding<?>[] parameterBindings = Stream.concat(
        dirtyFields.stream()
          .map(field -> bindFieldParameter(field.fieldMapping(), field.value())),
        Stream.of(entityKey)
          .map(key -> bindParameter(key.id(), primaryKey.getJdbcType())))
      .toArray(JdbcParameterBinding[]::new);

    int updatedRows = session.getJdbcExecutor().executeUpdate(updateStatement, parameterBindings);

    if (updatedRows == 0) {
      throw new OptimisticLockException("Row was updated by another transaction " + entityKey);
    }

    log.debug("Entity with id {} was updated in table {}, updated rows: {}", entityKey, tableName, updatedRows);
  }

  public String getLockModeSqlAppend(LockMode lockModeEnum) {

    switch (lockModeEnum) {
      case FOR_SHARE:
        return FOR_SHARE;
      case FOR_UPDATE:
        return FOR_UPDATE;
      default:
        return NONE;
    }
  }

  private <T> void initOptimisticLock(T entity, FieldMapping<?> optimisticLock) {
    Number version = 1;
    if (Long.class.isAssignableFrom(optimisticLock.getFieldType())) {
      version = 1L;
    }

    EntityUtils.setFieldValue(optimisticLock.getFieldName(), entity, version);
  }

  private Number getOptimisticLockNextValue(Number optimisticLockPrevValue) {
    Number optimisticLockNextValue = 0;
    if (optimisticLockPrevValue instanceof Integer prevIntValue) {
      optimisticLockNextValue = prevIntValue + 1;
    } else if (optimisticLockPrevValue instanceof Long prevLongValue) {
      optimisticLockNextValue = prevLongValue + 1;
    }
    return optimisticLockNextValue;
  }

  private boolean isIdentityGenerated(FieldMapping<?> primaryKeyMapping) {
    return primaryKeyMapping.getIdGeneratorStrategy().isIdentityGenerated();
  }

  private <T> JdbcParameterBinding<?>[] getInsertParameterBinding(T entity, List<FieldMapping<?>> insertableFields) {
    return insertableFields.stream().map(fieldMapping -> {
        Object fieldValue = EntityUtils.getFieldValue(fieldMapping.getFieldName(), entity);
        return bindFieldParameter(fieldMapping, fieldValue);
      }).toList()
      .toArray(new JdbcParameterBinding<?>[0]);
  }

  /**
   * Generates a comma-separated string of column names to be updated based on the dirty fields provided.
   * This method takes an array of {@link DirtyFieldMapping} objects as input, which represent the dirty fields of an entity.
   * It streams over the array, maps each {@link DirtyFieldMapping} to its associated {@link FieldMapping}, and then maps each {@link FieldMapping} to a
   * string representation of the column name to be updated.
   * The string is formatted as "columnName=?" for each column that needs to be updated.
   * The resulting strings are then joined together into a single comma-separated string.
   *
   * @param dirtyFields An array of {@link DirtyFieldMapping} objects representing the dirty fields of an entity.
   *
   * @return A comma-separated string of column names to be updated, each formatted as "columnName=?".
   */
  private String getColumnsToUpdate(List<DirtyFieldMapping<Object>> dirtyFields) {
    return dirtyFields.stream()
      .map(DirtyFieldMapping::fieldMapping)
      .map(fieldMapping -> fieldMapping.getColumnName() + "=?")
      .collect(joining(","));
  }

  private String getColumnNames(List<FieldMapping<?>> fields) {
    return fields.stream()
      .map(FieldMapping::getColumnName)
      .collect(Collectors.joining(", "));
  }

  private String generatePlaceholders(Collection<?> collection) {
    return String.join(", ", Collections.nCopies(collection.size(), "?"));
  }

  private <T> boolean isDetached(T entity) {
    return isDetached(session.getEntityDetails(entity).entityKey());
  }

  private <T> boolean isDetached(EntityKey<T> entityKey) {
    return session.getPersistenceContext().isDetached(entityKey);
  }

}

