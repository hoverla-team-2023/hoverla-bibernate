package com.bibernate.hoverla.jdbc.types;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * The PostgreSqlJdbcEnumType class is an implementation of the {@link BibernateJdbcType} interface, specifically
 * designed for working with PostgreSQL ENUM types. It allows for extracting and binding values of a
 * specific enum type T to/from a ResultSet and PreparedStatement for SQL queries and updates involving
 * ENUM columns in PostgreSQL.
 *
 * @param <T> The enum type to work with in the JDBC context.
 */
public class PostgreSqlJdbcEnumType<T extends Enum<T>> implements BibernateJdbcType<T> {

  private final Class<T> enumClass;

  /**
   * Constructs a new instance of PostgreSqlJdbcEnumType for the specified enumClass.
   *
   * @param enumClass The enum class representing the ENUM type in PostgreSQL.
   */
  public PostgreSqlJdbcEnumType(Class<T> enumClass) {
    this.enumClass = enumClass;
  }

  /**
   * Extracts an enum value of type T from the specified ResultSet at the given columnIndex,
   * mapping it to the corresponding enum constant.
   *
   * @param resultSet   The ResultSet from which to extract the enum value.
   * @param columnIndex The index of the column in the ResultSet containing the ENUM value.
   *
   * @return The extracted enum value of type T, or null if the value is null in the ResultSet.
   *
   * @throws SQLException If an SQL exception occurs while extracting the data.
   */
  @Override
  public T extractData(ResultSet resultSet, int columnIndex) throws SQLException {
    Object object = resultSet.getObject(columnIndex);
    return object != null ? Enum.valueOf(enumClass, (String) object) : null;
  }

  /**
   * Binds an enum value of type T to the specified PreparedStatement at the given index, setting it
   * as an ENUM type with the Types.OTHER SQL type.
   *
   * @param preparedStatement The PreparedStatement to which the enum value will be bound.
   * @param index             The index at which to bind the enum value in the PreparedStatement.
   * @param value             The enum value of type T to be bound to the PreparedStatement.
   *
   * @throws SQLException If an SQL exception occurs while binding the parameter.
   */
  @Override
  public void bindParameter(PreparedStatement preparedStatement, int index, T value) throws SQLException {
    preparedStatement.setObject(index, value, Types.OTHER);
  }

}