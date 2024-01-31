package com.bibernate.hoverla.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to define a column in a table.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {

  /**
   * The name of the column. If not specified, the name of the field in snake case will be used.
   */
  String name() default "";

  /**
   * Whether the column can be used in INSERT statements.
   */
  boolean insertable() default true;

  /**
   * Whether the column can be used in UPDATE statements.
   */
  boolean updatable() default true;

  /**
   * Whether the column is nullable.
   */
  boolean nullable() default true;

  /**
   * Whether the column should be unique
   */
  boolean unique() default false;

}
