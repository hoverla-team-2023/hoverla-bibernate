package com.bibernate.hoverla.session;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.NotImplementedException;

import com.bibernate.hoverla.exceptions.BibernateException;
import com.bibernate.hoverla.jdbc.JdbcParameterBinding;
import com.bibernate.hoverla.jdbc.JdbcResultExtractor;
import com.bibernate.hoverla.metamodel.EntityMapping;
import com.bibernate.hoverla.metamodel.FieldMapping;
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
  private static final String UPDATE_TABLE_BY_ID = "UPDATE %s SET %s WHERE %s = ?;";

  private final SessionImplementor sessionImplementor;

  public <T> T insert(T entity) {
    throw new NotImplementedException();//todo
  }

  public <T> void update(T entity) {

    var entityDetails = sessionImplementor.getEntityDetails(entity);
    var entityKey = entityDetails.entityKey();

    log.debug("Updating entity: {}", entityKey);

    DirtyFieldMapping<?>[] dirtyFields = sessionImplementor.getPersistenceContext().getUpdatedFields(entity);

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

    int updatedRows = sessionImplementor.getJdbcExecutor().executeUpdate(updateStatement, parameterBindings);

    log.debug("Entity with id {} was updated in table {}, updated rows {}", entityKey, tableName, updatedRows);

    if (updatedRows == 0) {
      throw new BibernateException("Row was updated by another transaction " + entityKey);
    }
  }

  public <T> void delete(T entity) {

    EntityDetails entityDetails = sessionImplementor.getEntityDetails(entity);

    log.debug("Deleting entity: {}", entityDetails.entityKey());

    FieldMapping<?> primaryKeyMapping = entityDetails.entityMapping().getPrimaryKeyMapping();
    String deleteStatement = DELETE_FROM_TABLE_BY_ID.formatted(entityDetails.entityMapping().getTableName(),
                                                               primaryKeyMapping.getColumnName());

    JdbcParameterBinding<?>[] bindValues = { bindParameter(entityDetails.entityKey().id(),
                                                           primaryKeyMapping.getJdbcType()) };

    int updatedRows = sessionImplementor.getJdbcExecutor().executeUpdate(deleteStatement, bindValues);

    log.debug("Entity with id {} deleted from table {}, updated rows {}", entityDetails.entityKey(), entityDetails.entityMapping().getTableName(),
              updatedRows);

    if (updatedRows == 0) {
      throw new BibernateException("Row was deleted by another transaction " + entityDetails.entityKey());
    }

  }

  public <T> T load(EntityKey<T> entityKey) {

    EntityMapping entityMapping = sessionImplementor.getEntityMapping(entityKey.entityType());

    FieldMapping<?> primaryKeyMapping = entityMapping.getPrimaryKeyMapping();

    var columnNames = EntityUtils.getColumnNames(entityMapping);
    String selectStatement = SELECT_FROM_TABLE_BY_ID.formatted(columnNames,
                                                               entityMapping.getTableName(),
                                                               primaryKeyMapping.getColumnName());

    JdbcParameterBinding<?>[] bindValues = { bindParameter(entityKey.id(),
                                                           primaryKeyMapping.getJdbcType()) };
    var jdbcResultExtractors = EntityUtils.getJdbcTypes(entityMapping);

    List<Object[]> rows = sessionImplementor.getJdbcExecutor().executeSelectQuery(selectStatement,
                                                                                  bindValues,
                                                                                  jdbcResultExtractors.toArray(new JdbcResultExtractor<?>[0]));

    List<T> results = rows.stream()
      .map(row -> sessionImplementor
        .getEntityRowMapper()
        .createEntityFromRow(row, entityKey.entityType()))
      .toList();

    if (results.size() > 1) {
      String errorMessage = "Multiple entities found for the given entity key : %s. Expected only one result.".formatted(entityKey);
      throw new BibernateException(errorMessage);
    }

    return results.stream().findFirst().orElse(null);
  }

  private String getColumnsToUpdate(DirtyFieldMapping<?>[] dirtyFields) {
    return Arrays.stream(dirtyFields)
      .map(DirtyFieldMapping::fieldMapping)
      .map(fieldMapping -> fieldMapping.getColumnName() + "=?")
      .collect(joining(","));
  }

  private Object createEntityFromRow(Object[] row, EntityMapping entityMapping) {
    var entity = EntityUtils.newInstanceOf(entityMapping.getEntityClass());
    int i = 0;
    for (var value : entityMapping.getFieldMappingMap().values()) {
      EntityUtils.setFieldValue(value.getFieldName(), entity, row[i++]);
    }
    return entity;
  }

}

