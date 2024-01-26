package com.bibernate.hoverla.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * The JdbcParameterBinder interface defines a contract for binding a value of type T to a
 * PreparedStatement at a specified index in a SQL query using JDBC.
 *
 * @param <T> The type of the value to be bound.
 */
public interface JdbcParameterBinder<T> {

  /**
   * Binds the specified value to the provided PreparedStatement at the specified index.
   *
   * @param preparedStatement The PreparedStatement to which the value will be bound.
   * @param index             The index at which to bind the value in the PreparedStatement.
   * @param value             The value of type T to be bound to the PreparedStatement.
   *
   * @throws SQLException If an SQL exception occurs while binding the parameter.
   */
  void bindParameter(PreparedStatement preparedStatement, int index, T value) throws SQLException;

}
