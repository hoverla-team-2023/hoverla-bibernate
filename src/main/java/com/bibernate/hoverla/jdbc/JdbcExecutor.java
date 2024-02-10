package com.bibernate.hoverla.jdbc;

import java.util.List;

import com.bibernate.hoverla.exceptions.BibernateSqlException;

/**
 * The JdbcExecutor interface provides methods for executing SQL queries and updates using JDBC.
 * It allows to perform various database operations, including select queries, insertions, updates,
 * deletions, and working with generated keys.
 */
public interface JdbcExecutor {

  /**
   * Executes a select query with the given SQL template, parameter bindings, and result extractors.
   *
   * @param sqlTemplate      The SQL query template to be executed.
   * @param bindValues       An array of parameter bindings for the SQL query.
   * @param resultExtractors An array of result extractors for processing the query result.
   * @return A list of Object arrays representing the query results.
   * @throws BibernateSqlException If an SQL exception occurs during query execution.
   */
  List<Object[]> executeSelectQuery(String sqlTemplate,
                                    JdbcParameterBinding<?>[] bindValues,
                                    JdbcResultExtractor<?>[] resultExtractors);

  /**
   * Executes an update, insert, or delete query with the given SQL template and parameter bindings
   * and returns generated keys if available.
   *
   * @param sqlTemplate The SQL update, insert, or delete query template to be executed.
   * @param bindValues  An array of parameter bindings for the SQL query.
   * @return An Object representing the generated keys, if available, or null otherwise.
   * @throws BibernateSqlException If an SQL exception occurs during query execution.
   */
  Object executeUpdateAndReturnGeneratedKeys(String sqlTemplate,
                                             JdbcParameterBinding<?>[] bindValues);

  /**
   * Executes an update, insert, or delete query with the given SQL template and parameter bindings
   * and returns the number of rows affected.
   *
   * @param sqlTemplate The SQL update, insert, or delete query template to be executed.
   * @param bindValues  An array of parameter bindings for the SQL query.
   * @return The number of rows affected by the update, insert, or delete operation.
   * @throws BibernateSqlException If an SQL exception occurs during query execution.
   */
  int executeUpdate(String sqlTemplate, JdbcParameterBinding<?>[] bindValues);
}
