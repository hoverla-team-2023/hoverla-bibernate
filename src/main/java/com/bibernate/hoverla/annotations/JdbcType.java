package com.bibernate.hoverla.annotations;

/**
 * Helper annotation to explicitly specify {@link com.bibernate.hoverla.jdbc.types.JdbcType JDBC type} for column mapping
 */
public @interface JdbcType {

  Class<? extends com.bibernate.hoverla.jdbc.types.JdbcType<?>> value();

}
