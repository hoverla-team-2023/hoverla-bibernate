package com.bibernate.hoverla.session;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.bibernate.hoverla.exceptions.BibernateException;
import com.bibernate.hoverla.exceptions.PersistOperationException;
import com.bibernate.hoverla.jdbc.JdbcParameterBinding;
import com.bibernate.hoverla.jdbc.JdbcResultExtractor;
import com.bibernate.hoverla.metamodel.EntityMapping;
import com.bibernate.hoverla.metamodel.FieldMapping;
import com.bibernate.hoverla.metamodel.OneToManyMapping;
import com.bibernate.hoverla.session.cache.CollectionKey;
import com.bibernate.hoverla.session.cache.EntityKey;
import com.bibernate.hoverla.session.dirtycheck.DirtyFieldMapping;
import com.bibernate.hoverla.utils.EntityUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static java.util.stream.Collectors.joining;

import static com.bibernate.hoverla.jdbc.JdbcParameterBinding.bindParameter;

@Slf4j
@RequiredArgsConstructor
public class EntityDaoService {

  private static final String DELETE_FROM_TABLE_BY_ID = "DELETE FROM %s WHERE %s = ?;";
  private static final String SELECT_FROM_TABLE_BY_ID = "SELECT %s FROM %s WHERE %s = ?;";
  private static final String INSERT_INTO_TABLE = "INSERT INTO %s (%s) VALUES (%s);";
  private static final String UPDATE_TABLE_BY_ID = "UPDATE %s SET %s WHERE %s = ?;";

  private final SessionImplementor session;

  public <T> void insert(T entity) {
    EntityMapping entityMapping = session.getEntityMapping(entity.getClass());

    List<FieldMapping<?>> insertableFields = getInsertableFields(entityMapping);
    JdbcParameterBinding<?>[] parameterBindings = getInsertParameterBinding(entity, insertableFields);
    FieldMapping<?> primaryKeyMapping = entityMapping.getPrimaryKeyMapping();

    String insertStatement = INSERT_INTO_TABLE.formatted(
      entityMapping.getTableName(),
      getColumnNames(insertableFields),
      generatePlaceholders(insertableFields)
    );

    if (!isIdentityGenerated(primaryKeyMapping)) {
      int updatedRows = session.getJdbcExecutor().executeUpdate(insertStatement, parameterBindings);
      verifyInsertOperation(updatedRows);
      return;
    }

    Object generatedKey = session.getJdbcExecutor()
      .executeUpdateAndReturnGeneratedKeys(insertStatement, parameterBindings, primaryKeyMapping.getJdbcType());

    EntityUtils.setFieldValue(primaryKeyMapping.getFieldName(), entity, generatedKey);
  }

  public <T> List<T> loadCollection(CollectionKey<?> collectionKey) {
    EntityMapping entityMappingOfParent = session.getEntityMapping(collectionKey.entityType());
    FieldMapping<?> fieldMapping = entityMappingOfParent.getFieldMapping(collectionKey.collectionName());
    OneToManyMapping oneToManyMapping = fieldMapping.getOneToManyMapping();

    Class<T> entityType = (Class<T>) oneToManyMapping.getCollectionType();
    EntityMapping entityMapping = session.getEntityMapping(entityType);
    FieldMapping<?> joinColumn = entityMapping.getFieldMapping(oneToManyMapping.getMappedBy());

    String selectStatement = SELECT_FROM_TABLE_BY_ID.formatted(EntityUtils.getColumnNames(entityMapping),
                                                               entityMapping.getTableName(),
                                                               joinColumn.getColumnName());

    JdbcParameterBinding<?>[] bindValues = { bindParameter(collectionKey.id(),
                                                           joinColumn.getJdbcType()) };

    var jdbcResultExtractors = EntityUtils.getJdbcTypes(entityMapping);

    List<Object[]> rows = session.getJdbcExecutor().executeSelectQuery(selectStatement,
                                                                       bindValues,
                                                                       jdbcResultExtractors.toArray(new JdbcResultExtractor<?>[0]));
    log.debug("Creating collection {}", entityType);

    return rows.stream()
      .map(row -> session.getEntityRowMapper()
        .createEntityFromRow(row, entityType))
      .collect(Collectors.toList());
  }

  private void verifyInsertOperation(int updatedRows) {
    if (updatedRows == 0) {
      throw new PersistOperationException("Row was not persisted");
    }
  }

  public <T> void update(T entity) {

    var entityDetails = session.getEntityDetails(entity);
    var entityKey = entityDetails.entityKey();

    log.debug("Updating entity: {}", entityKey);

    DirtyFieldMapping<?>[] dirtyFields = session.getPersistenceContext().getUpdatedFields(entity);

    var entityMapping = entityDetails.entityMapping();
    FieldMapping<?> primaryKeyMapping = entityMapping.getPrimaryKeyMapping();

    String tableName = entityMapping.getTableName();
    String columnsToUpdate = getColumnsToUpdate(dirtyFields);

    String updateStatement = UPDATE_TABLE_BY_ID.formatted(
      tableName,
      columnsToUpdate,
      primaryKeyMapping.getColumnName()
    );

    JdbcParameterBinding<?>[] parameterBindings = Stream.concat(
        Arrays.stream(dirtyFields)
          .map(field -> bindParameter(field.value(), field.fieldMapping().getJdbcType())),
        Stream.of(entityKey)
          .map(key -> bindParameter(key.id(), primaryKeyMapping.getJdbcType())))
      .toArray(JdbcParameterBinding[]::new);

    int updatedRows = session.getJdbcExecutor().executeUpdate(updateStatement, parameterBindings);

    log.debug("Entity with id {} was updated in table {}, updated rows {}", entityKey, tableName, updatedRows);

    if (updatedRows == 0) {
      throw new BibernateException("Row was updated by another transaction " + entityKey);
    }
  }

  public <T> void delete(T entity) {

    EntityDetails entityDetails = session.getEntityDetails(entity);

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

  public <T> T load(EntityKey<T> entityKey) {

    EntityMapping entityMapping = session.getEntityMapping(entityKey.entityType());

    FieldMapping<?> primaryKeyMapping = entityMapping.getPrimaryKeyMapping();

    var columnNames = EntityUtils.getColumnNames(entityMapping);
    String selectStatement = SELECT_FROM_TABLE_BY_ID.formatted(columnNames,
                                                               entityMapping.getTableName(),
                                                               primaryKeyMapping.getColumnName());

    JdbcParameterBinding<?>[] bindValues = { bindParameter(entityKey.id(),
                                                           primaryKeyMapping.getJdbcType()) };

    var jdbcResultExtractors = EntityUtils.getJdbcTypes(entityMapping);

    List<Object[]> rows = session.getJdbcExecutor().executeSelectQuery(selectStatement,
                                                                       bindValues,
                                                                       jdbcResultExtractors.toArray(new JdbcResultExtractor<?>[0]));

    List<T> results = rows.stream()
      .map(row -> session.getEntityRowMapper()
        .createEntityFromRow(row, entityKey.entityType()))
      .toList();

    if (results.size() > 1) {
      String errorMessage = "Multiple entities found for the given entity key : %s. Expected only one result.".formatted(entityKey);
      throw new BibernateException(errorMessage);
    }

    return results.stream().findFirst().orElse(null);
  }

  private boolean isIdentityGenerated(FieldMapping<?> primaryKeyMapping) {
    return primaryKeyMapping.getIdGeneratorStrategy().isIdentityGenerated();
  }

  private List<FieldMapping<?>> getInsertableFields(EntityMapping entityMapping) {
    return entityMapping.getFieldNameMappingMap()
      .values().stream()
      .filter(FieldMapping::isInsertable).toList();
  }

  private <T> JdbcParameterBinding<?>[] getInsertParameterBinding(T entity, List<FieldMapping<?>> insertableFields) {
    return insertableFields.stream().map(fieldMapping -> {
        Object fieldValue = EntityUtils.getFieldValue(fieldMapping.getFieldName(), entity);
        return bindParameter(fieldValue, fieldMapping.getJdbcType());
      }).toList()
      .toArray(new JdbcParameterBinding<?>[0]);
  }

  private String getColumnsToUpdate(DirtyFieldMapping<?>[] dirtyFields) {
    return Arrays.stream(dirtyFields)
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

}

