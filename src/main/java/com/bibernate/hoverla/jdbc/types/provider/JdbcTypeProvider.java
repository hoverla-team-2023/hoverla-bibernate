package com.bibernate.hoverla.jdbc.types.provider;

import javax.annotation.Nullable;

import com.bibernate.hoverla.jdbc.types.JdbcType;

/**
 * Provider for {@link JdbcType} instances.
 */
public interface JdbcTypeProvider {

  /**
   * Return a {@link JdbcType} instance based on the provided <code>jdbcTypeClass</code>. If <code>jdbcTypeClass</code> is <code>null</code>,
   * then resolve the resulting JdbcType from the <code>fieldType</code>.
   *
   * @param jdbcTypeClass class of the {@link JdbcType} to be returned
   * @param fieldType     class of the entity field
   * @param <T>           type of the entity field
   *
   * @return {@link JdbcType} instance
   */
  <T> JdbcType<T> getInstance(@Nullable Class<? extends JdbcType<T>> jdbcTypeClass, Class<? extends T> fieldType);

}
