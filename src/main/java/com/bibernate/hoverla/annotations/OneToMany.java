package com.bibernate.hoverla.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field as representing a one-to-many relationship in a custom entity.
 * This annotation should be used on a collection-valued field in an entity
 * class to define a one-to-many mapping between entities.
 *
 * <p>Example Usage:</p>
 * <pre>{@code
 * public class Team {
 *     private Long id;
 *     private String name;
 *
 *     @OneToMany(mappedBy = "team")
 *     private List<Player> players;
 *
 *     // Getters and setters
 * }
 * }</pre>
 *
 * <p>In this example, the {@code players} field in the {@code Team} entity is marked with
 * {@code @OneToMany(mappedBy = "team")}, indicating a one-to-many relationship between the
 * {@code Team} and {@code Player} entities. The {@code mappedBy} attribute specifies the
 * field in the {@code Player} entity that owns the relationship.</p>
 *
 * <p><strong>Note:</strong> This annotation should only be used for bidirectional associations.</p>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OneToMany {

  String mappedBy();

}