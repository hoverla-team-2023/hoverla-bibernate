package com.bibernate.hoverla.jdbc.types;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DefaultBibernateJdbcTypeImpl<T> implements BibernateJdbcType<T> {

  @Override
  public void bindParameter(PreparedStatement preparedStatement, int index, Object object) throws SQLException {
    preparedStatement.setObject(index, object);
  }

  @Override
  public T extractData(ResultSet resultSet, int index) throws SQLException {
    return (T) resultSet.getObject(index);
  }

}
