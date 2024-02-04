package com.bibernate.hoverla.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import lombok.AllArgsConstructor;
import lombok.ToString;

/**
 * The JdbcParameterBinding class represents a parameter binding for a SQL query using JDBC.
 * It associates a bind value with a specific binder responsible for setting the value in a
 * PreparedStatement. This class is commonly used in conjunction with the {@link JdbcExecutor}
 * interface for executing SQL queries and updates.
 *
 * @param <T> The type of the bind value.
 */
@AllArgsConstructor
@ToString
public class JdbcParameterBinding<T> {

  private T bindValue;
  private JdbcParameterBinder<T> binder;

  /**
   * Binds a value to a JDBC parameter using a specified JDBC parameter binder.
   *
   * @param bindValue           The value to bind to the JDBC parameter.
   * @param jdbcParameterBinder The JDBC parameter binder responsible for binding the value.
   * @param <T>                 The type of the value to be bound.
   *
   * @return A JDBC parameter binding containing the bound value and binder.
   */
  @SuppressWarnings("unchecked")
  public static <T> JdbcParameterBinding<?> bindParameter(Object bindValue, JdbcParameterBinder<?> jdbcParameterBinder) {
    return new JdbcParameterBinding<>((T) bindValue, (JdbcParameterBinder<T>) jdbcParameterBinder);
  }

  /**
   * Binds the associated bind value to the provided PreparedStatement at the specified index
   * using the associated binder.
   *
   * @param preparedStatement The PreparedStatement to which the bind value will be bound.
   * @param index             The index at which to bind the value in the PreparedStatement.
   *
   * @throws SQLException If an SQL exception occurs while binding the parameter.
   */
  public void doBind(PreparedStatement preparedStatement, int index) throws SQLException {
    binder.bindParameter(preparedStatement, index, bindValue);
  }

}


