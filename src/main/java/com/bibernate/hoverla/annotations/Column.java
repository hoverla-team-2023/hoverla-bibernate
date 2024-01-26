package com.bibernate.hoverla.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// todo: add JavaDoc
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {

  String name() default "";

  boolean insertable() default true;

  boolean updatable() default true;

  //  could be used during mapping a result set to entity
  boolean nullable() default true;

  // could be used during batch insertions
  boolean unique() default false;

}
