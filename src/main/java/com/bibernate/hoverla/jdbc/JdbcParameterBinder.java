package com.bibernate.hoverla.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface JdbcParameterBinder<T> {

  void bindParameter(PreparedStatement preparedStatement, int index, T value) throws SQLException;

}
