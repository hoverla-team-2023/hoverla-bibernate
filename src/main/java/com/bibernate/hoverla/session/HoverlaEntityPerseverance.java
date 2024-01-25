package com.bibernate.hoverla.session;

import javax.sql.DataSource;

import com.bibernate.hoverla.session.impl.SessionImpl;

import lombok.extern.slf4j.Slf4j;

/**
 * The HoverlaEntityPerseverance class is responsible for persisting entities to the database using Hibernate-like operations.
 * It works in conjunction with the provided DataSource, SessionImpl, and PersistenceContext.
 */

@Slf4j
public class HoverlaEntityPerseverance {

  public HoverlaEntityPerseverance(DataSource dataSource, SessionImpl session, PersistenceContext persistenceContext) {

  }
  //  private final DataSource dataSource;
  //  private final SessionImpl session;
  //  private final PersistenceContext persistenceContext;
  //
  //
  //  @SneakyThrows
  //  public <T> T insert(T entity) {
  //    log.trace("Inserting entity {}", entity);
  //    var entityType = entity.getClass();
  //    try (var connection = dataSource.getConnection()) {
  //      var tableName = resolveTableName(entityType);
  //      log.trace("Resolved table name -> '{}'", tableName);
  //      var columns = commaSeparatedInsertableColumns(entityType);
  //      var params = commaSeparatedInsertableParams(entityType);
  //      var insertQuery = String.format(INSERT_INTO_TABLE_VALUES_TEMPLATE, tableName, columns, params);
  //      log.trace("Preparing insert statement: {}", insertQuery);
  //      try (var insertStatement = connection.prepareStatement(insertQuery)) {
  //        fillInsertStatementParams(insertStatement, entity);
  //        log.debug("SQL: {}", insertStatement);
  //        insertStatement.executeUpdate();
  //      }
  //    }
  //    return entity;
  //  }

}


