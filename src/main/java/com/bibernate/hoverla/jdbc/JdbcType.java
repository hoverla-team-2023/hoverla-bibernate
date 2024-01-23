package com.bibernate.hoverla.jdbc;

public interface JdbcType<T> extends JdbcResultExtractor<T>, JdbcParameterBinder<T> {
}