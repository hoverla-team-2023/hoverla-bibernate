package com.bibernate.hoverla.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a field should be excluded from persistence in the database.
 * Fields annotated with {@code @Transient} will not be mapped to database columns
 * and will be ignored during persistence operations.
 *
 * <p>Example Usage:</p>
 * <pre>{@code
 * @Entity
 * @Table(name = "employee")
 * public class Employee {
 *     @Id
 *     private Long id;
 *
 *     @Transient
 *     private String note;
 *
 *     // Getters and setters, other fields
 * }
 * }</pre>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Transient {}
