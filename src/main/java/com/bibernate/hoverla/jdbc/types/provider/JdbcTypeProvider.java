package com.bibernate.hoverla.jdbc.types.provider;

import javax.annotation.Nullable;

import com.bibernate.hoverla.jdbc.types.BibernateJdbcType;

/**
 * Provider for {@link BibernateJdbcType} instances.
 */
public interface JdbcTypeProvider {

  /**
   * Return a {@link BibernateJdbcType} instance based on the provided <code>jdbcTypeClass</code>. If <code>jdbcTypeClass</code> is <code>null</code>,
   * then resolve the resulting JdbcType from the <code>fieldType</code>.
   *
   * @param jdbcTypeClass class of the {@link BibernateJdbcType} to be returned
   * @param fieldType     class of the entity field
   * @param <T>           type of the entity field
   *
   * @return {@link BibernateJdbcType} instance
   */
  <T> BibernateJdbcType<T> getInstance(@Nullable Class<? extends BibernateJdbcType<T>> jdbcTypeClass, Class<?> fieldType);

}
