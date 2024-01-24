package com.bibernate.hoverla.jdbc.types;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class PostgreSqlJdbcEnumType<T extends Enum<T>> implements JdbcType<T> {

  private final Class<T> enumClass;

  public PostgreSqlJdbcEnumType(Class<T> enumClass) {
    this.enumClass = enumClass;
  }

  @Override
  public T extractData(ResultSet resultSet, int columnIndex) throws SQLException {
    Object object = resultSet.getObject(columnIndex);
    return object != null ? Enum.valueOf(enumClass, (String) object) : null;
  }

  @Override
  public void bindParameter(PreparedStatement preparedStatement, int index, T value) throws SQLException {
    preparedStatement.setObject(index, value, Types.OTHER);
  }

}
