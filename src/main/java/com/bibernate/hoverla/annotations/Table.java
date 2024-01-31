package com.bibernate.hoverla.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Helper annotation to explicitly specify table name for entity mapping.
 * <p/>
 * If not specified, the table name will be derived from the class name. The default strategy is to convert the class name to snake case.
 * For example, for the class <code>Authors</code> the table name will be <code>authors</code>, and for the class <code>BookAuthors</code> the table name will
 * be resolved as <code>book_authors</code>.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Table {

  String value();

}
