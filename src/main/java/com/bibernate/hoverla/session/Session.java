package com.bibernate.hoverla.session;

import com.bibernate.hoverla.session.transaction.Transaction;

/**
 * The `Session` interface represents a single unit of work with the database.
 * It extends `AutoCloseable` to allow for automatic resource management.
 */
public interface Session extends AutoCloseable {

  <T> T find(Class<T> entityClass, Object id);

  <T> void persist(T entity);

  <T> T merge(T entity);

  void detach(Object entity);

  void delete(Object entity);

  void flush();

  void close();

  Transaction getTransaction();

}
