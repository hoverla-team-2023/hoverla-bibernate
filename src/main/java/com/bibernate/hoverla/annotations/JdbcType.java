package com.bibernate.hoverla.annotations;

import com.bibernate.hoverla.jdbc.JdbcDataType;

/**
 * Helper annotation to explicitly specify {@link JdbcDataType JDBC type} for column mapping
 */
public @interface JdbcType {

  JdbcDataType value();

}
