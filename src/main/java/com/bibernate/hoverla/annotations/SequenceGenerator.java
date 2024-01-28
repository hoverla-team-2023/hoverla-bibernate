package com.bibernate.hoverla.annotations;

public @interface SequenceGenerator {

  /**
   * The name of the sequence generator
   */
  String name();
  /**
   * The name of the sequence in the database
   */
  String sequenceName();

  /**
   * The value to increment the sequence by
   */
  int allocationSize() default 1;

}