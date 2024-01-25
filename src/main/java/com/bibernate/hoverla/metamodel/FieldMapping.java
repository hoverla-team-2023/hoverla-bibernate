package com.bibernate.hoverla.metamodel;

import com.bibernate.hoverla.jdbc.JdbcType;

public class FieldMapping<T> {

  private String columnName;
  private JdbcType<T> jdbcType;
  private Class<T> fieldType;
  private String fieldName;
  private boolean isInsertable;
  private boolean isUpdatable;
  private boolean isNullable;
  private boolean isUnique;
  private boolean isPrimaryKey;
  // todo: probably add fields like isLazy, mappingTo, mappedBy

}
