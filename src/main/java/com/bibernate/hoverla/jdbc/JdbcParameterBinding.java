package com.bibernate.hoverla.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class JdbcParameterBinding<T> {

  private T bindValue;
  private JdbcParameterBinder<T> binder;

  public void doBind(PreparedStatement preparedStatement, int index) throws SQLException {
    binder.bindParameter(preparedStatement, index, bindValue);
  }

}
