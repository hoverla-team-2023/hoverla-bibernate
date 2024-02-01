package com.bibernate.hoverla.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Set;
import java.util.UUID;

/**
 * Indicates that the annotated field should be treated as the primary key. Note that it is allowed
 * to specify <code>@Id</code> on multiple fields for composite primary keys.
 *
 * @see GeneratedValue
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Id {
  Set<Class<?>> SUPPORTED_OBJECT_TYPES =
    Set.of(
      Integer.class,
      Long.class,
      UUID.class,
      String.class,
      BigDecimal.class,
      BigInteger.class,
      java.util.Date.class,
      java.sql.Date.class,
      int.class,
      long.class);
}
