package com.bibernate.hoverla.session;

public interface Session extends AutoCloseable {

  <T> T find(Class<T> entityClass, Object id);

  <T> void persist(T entity);

  <T> T merge(T entity);

  void detach(Object entity);

  void delete(Object entity);

  void flush();

  void close();

}
