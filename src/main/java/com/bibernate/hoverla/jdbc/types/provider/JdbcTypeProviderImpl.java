package com.bibernate.hoverla.jdbc.types.provider;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.annotation.Nullable;

import com.bibernate.hoverla.exceptions.BibernateException;
import com.bibernate.hoverla.jdbc.types.BibernateJdbcType;
import com.bibernate.hoverla.jdbc.types.DefaultBibernateJdbcTypeImpl;

/**
 * An implementation of the {@link JdbcTypeProvider} interface that provides instances of {@link BibernateJdbcType}.
 */
public class JdbcTypeProviderImpl implements JdbcTypeProvider {

  /**
   * Returns an instance of a {@link BibernateJdbcType} for the given class and field type.
   * If the provided class is not null, it attempts to instantiate the class using its declared constructor.
   * If the constructor requires a single parameter, it is invoked with the field type.
   * If the constructor does not require any parameters, it is invoked without any arguments.
   * If the instantiation fails, a {@link BibernateException} is thrown.
   * If the provided class is null, a default {@link DefaultBibernateJdbcTypeImpl} is returned.
   *
   * @param jdbcTypeClass The class of the {@link BibernateJdbcType} to instantiate.
   * @param fieldType The class of the field type for which to get the {@link BibernateJdbcType}.
   * @param <T> type of the object to be bound or extracted.
   * @return An instance of a {@link BibernateJdbcType} for the given class and field type.
   * @throws BibernateException If the instantiation of the {@link BibernateJdbcType} fails.
   */

  @Override
  @SuppressWarnings("unchecked")
  public <T> BibernateJdbcType<T> getInstance(@Nullable Class<? extends BibernateJdbcType<T>> jdbcTypeClass, Class<?> fieldType) {
    if (jdbcTypeClass != null) {
      try {
        Constructor<?> declaredConstructor = jdbcTypeClass.getDeclaredConstructors()[0];
        if (declaredConstructor.getParameterCount() == 1) {
          return (BibernateJdbcType<T>) declaredConstructor.newInstance(fieldType);
        }
        return jdbcTypeClass.getDeclaredConstructor().newInstance();
      } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
        throw new BibernateException(e);
      }
    }
    return (BibernateJdbcType<T>) new DefaultBibernateJdbcTypeImpl();
  }

}
