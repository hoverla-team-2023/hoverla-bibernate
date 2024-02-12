package com.bibernate.hoverla.metamodel;

import com.bibernate.hoverla.jdbc.types.BibernateJdbcType;

import lombok.Builder;
import lombok.Getter;

/**
 * Metamodel that represents scanned entity field. It contains information about the related table column, field type, field name,
 * whether the field is entity's primary key, nullable, unique. It also has info whether the field should be specified in insert and update commands
 *
 * @param <T> type of the field
 */
@Getter
@Builder
public class FieldMapping<T> {

  private String columnName;
  private BibernateJdbcType<?> jdbcType;
  private Class<T> fieldType;
  private String fieldName;
  private boolean isInsertable;
  private boolean isUpdatable;
  private boolean isNullable;
  private boolean isUnique;
  private boolean isPrimaryKey;
  private boolean isManyToOne;

  private IdGeneratorStrategy idGeneratorStrategy;

}
