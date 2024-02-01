package com.bibernate.hoverla.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This is an annotation used to mark a one-to-many relationship between two classes.
 *
 * <p>
 * When applied to a field in a class, this annotation indicates that the field represents a
 * one-to-many relationship between the annotated class and another class. The field should
 * typically be a collection type (e.g., List, Set) containing the instances of the other class.
 * </p>
 *
 * <p>
 * An example usage of this annotation would be:
 * <pre>
 *     &#064;OneToMany
 *     private List&lt;Order&gt; orders;
 * </pre>
 * In this example, the "orders" field represents a one-to-many relationship between the class
 * where this annotation is applied and the "Order" class.
 * </p>
 *
 * <p>
 * The retention policy of this annotation is set to {@link RetentionPolicy#RUNTIME}, which means
 * it can be accessed and processed at runtime through reflection.
 * </p>
 *
 * <p>
 * This annotation can only be applied to fields.
 * </p>
 *
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface OneToMany {}
