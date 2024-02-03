package com.bibernate.hoverla.jdbc.types.provider;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.annotation.Nullable;

import com.bibernate.hoverla.exceptions.BibernateException;
import com.bibernate.hoverla.jdbc.types.BibernateJdbcType;
import com.bibernate.hoverla.jdbc.types.DefaultBibernateJdbcTypeImpl;

public class JdbcTypeProviderImpl implements JdbcTypeProvider {

  @Override
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
    return new DefaultBibernateJdbcTypeImpl<>();
  }

}
