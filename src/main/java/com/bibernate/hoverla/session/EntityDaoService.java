package com.bibernate.hoverla.session;

import org.apache.commons.lang3.NotImplementedException;

import com.bibernate.hoverla.exceptions.BibernateException;
import com.bibernate.hoverla.jdbc.JdbcParameterBinding;
import com.bibernate.hoverla.metamodel.FieldMapping;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.bibernate.hoverla.jdbc.JdbcParameterBinding.bindParameter;

@Slf4j
@RequiredArgsConstructor
public class EntityDaoService {

  private static final String DELETE_FROM_TABLE_BY_ID = "DELETE FROM %s WHERE %s = ?;";
  private final SessionImplementor sessionImplementor;

  public <T> T insert(T entity) {
    throw new NotImplementedException();//todo
  }

  public <T> T update(T entity) {
    throw new UnsupportedOperationException();//todo
  }

  public <T> void delete(T entity) {

    EntityDetails entityDetails = sessionImplementor.getEntityDetails(entity);

    log.debug("Deleting entity: {}", entityDetails.entityKey());

    FieldMapping<?> primaryKeyMapping = entityDetails.entityMapping().getPrimaryKeyMapping();
    String formatted = DELETE_FROM_TABLE_BY_ID.formatted(entityDetails.entityMapping().getTableName(),
                                                         primaryKeyMapping.getColumnName());

    JdbcParameterBinding<?>[] bindValues = { bindParameter(entityDetails.entityKey().id(),
                                                           primaryKeyMapping.getJdbcType()) };

    int updatedRows = sessionImplementor.getJdbcExecutor().executeUpdate(formatted, bindValues);

    log.debug("Entity with id {} deleted from table {}, updated rows {}", entityDetails.entityKey(), entityDetails.entityMapping().getTableName(),
              updatedRows);

    if (updatedRows == 0) {
      throw new BibernateException("Row was deleted by another transaction " + entityDetails.entityKey());
    }

  }

}

