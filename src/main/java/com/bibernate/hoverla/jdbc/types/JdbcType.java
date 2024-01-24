package com.bibernate.hoverla.jdbc.types;

import com.bibernate.hoverla.jdbc.JdbcParameterBinder;
import com.bibernate.hoverla.jdbc.JdbcResultExtractor;

public interface JdbcType<T> extends JdbcResultExtractor<T>, JdbcParameterBinder<T> {
}