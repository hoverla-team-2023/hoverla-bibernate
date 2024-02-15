package com.bibernate.hoverla.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.bibernate.hoverla.jdbc.types.BibernateJdbcType;

/**
 * Helper annotation to explicitly specify {@link BibernateJdbcType JDBC type} for column mapping.
 * <p>
 * The JdbcType annotation provides a flexible solution for handling JDBC types in Java entity classes.
 * It allows developers to customize JDBC type mappings for specific attributes, such as Postgres Enum types,
 * and introduce custom converters by implementing BibernateJdbcType implementations.
 * This flexibility empowers developers to tailor JDBC mappings and converters to meet the specific requirements of their application.
 * </p>
 * <p>
 * Example usage:
 * <pre>{@code
 * @Entity
 * public class Product {
 *     @JdbcType(PostgreSqlJdbcEnumType.class)
 *     private Color color;
 *     // Id, Other attributes, getters, setters
 * }
 * }</pre>
 * </p>
 */
@Target({ ElementType.FIELD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface JdbcType {

  /**
   * Class name of {@link BibernateJdbcType} implementation to use for column mapping
   */
  Class<? extends BibernateJdbcType> value();

}
