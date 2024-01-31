package com.bibernate.hoverla.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Helper annotation to explicitly specify {@link com.bibernate.hoverla.jdbc.types.JdbcType JDBC type} for column mapping
 */
@Target({ ElementType.FIELD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface JdbcType {

  /**
   * Class name of {@link com.bibernate.hoverla.jdbc.types.JdbcType} implementation to use for column mapping
   */
  Class<? extends com.bibernate.hoverla.jdbc.types.JdbcType> value();

}
