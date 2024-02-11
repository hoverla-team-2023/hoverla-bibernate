package com.bibernate.hoverla.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated field should be treated as the primary key. Note that it is allowed
 * to specify <code>@Id</code> on multiple fields for composite primary keys.
 *
 * @see IdentityGeneratedValue
 * @see SequenceGeneratedValue
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Id {
}
