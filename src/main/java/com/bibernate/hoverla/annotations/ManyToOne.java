package com.bibernate.hoverla.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The {@code ManyToOne} annotation is used to mark a field in a class as a many-to-one relationship.
 * A many-to-one relationship represents a relationship in which many instances of one class are associated with a single instance of another class.
 *
 * <p>This annotation is typically used in object-relational mapping (ORM) frameworks to indicate the mapping of a collection of entities into a single foreign key column in a database
 * table.
 * It is usually applied to a field in an entity class that represents the "many" side of the relationship.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * public class Order {
 *     @ManyToOne
 *     private Customer customer;
 *
 *     // ...
 * }
 * }</pre>
 *
 * <p>The use of this annotation can provide additional information to the ORM framework or other tools that might need to understand the association between entities.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ManyToOne {}
