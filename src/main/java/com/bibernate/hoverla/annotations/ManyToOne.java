package com.bibernate.hoverla.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field as representing a many-to-one or one-to-one relationship in a custom entity.
 * This annotation should be used on a field in an entity class to define a many-to-one or one-to-one mapping between entities.
 *
 * <p>Example Usage:</p>
 * <pre>
 * {@code
 * @Entity
 * public class Employee {
 *     @Id
 *     private Long id;
 *     private String name;
 *
 *     @ManyToOne
 *     private Department department;
 *
 *     // Getters and setters
 * }}
 * </pre>
 *
 * <p>In this example, the {@code department} field in the {@code Employee} entity is marked with
 * {@code @ManyToOne}, indicating either a many-to-one or a one-to-one relationship between the {@code Employee} and {@code Department} entities.</p>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ManyToOne {}