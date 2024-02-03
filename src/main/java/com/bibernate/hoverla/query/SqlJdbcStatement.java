package com.bibernate.hoverla.query;

import com.bibernate.hoverla.jdbc.JdbcParameterBinding;
import com.bibernate.hoverla.jdbc.JdbcResultExtractor;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SqlJdbcStatement<T> {

  String sqlTemplate;
  JdbcParameterBinding<?>[] getOrderedParameters;
  JdbcResultExtractor<?>[] jdbcResultExtractors;
}
