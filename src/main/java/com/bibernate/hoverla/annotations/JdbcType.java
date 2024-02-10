package com.bibernate.hoverla.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.bibernate.hoverla.jdbc.types.BibernateJdbcType;

/**
 * Helper annotation to explicitly specify {@link BibernateJdbcType JDBC type} for column mapping
 */
@Target({ ElementType.FIELD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface JdbcType {

  /**
   * Class name of {@link BibernateJdbcType} implementation to use for column mapping
   */
  Class<? extends BibernateJdbcType> value();

}
