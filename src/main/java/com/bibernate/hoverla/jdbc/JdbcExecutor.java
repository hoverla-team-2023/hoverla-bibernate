package com.bibernate.hoverla.jdbc;

import java.util.List;

public interface JdbcExecutor {

  List<Object[]> executeSelectQuery(String sqlTemplate, JdbcParameterBinding<?>[] bindValues, JdbcResultExtractor<?>[] resultExtractors);

  int executeUpdate(String sqlTemplate, JdbcParameterBinding<?>[] bindValues);

}
