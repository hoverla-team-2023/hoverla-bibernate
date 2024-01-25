package com.bibernate.hoverla.session;

/**
 * The `Session` interface represents a single unit of work with the database.
 * It extends `AutoCloseable` to allow for automatic resource management.
 */
public interface Session extends AutoCloseable {

  /**
   * Finds an entity of the specified class with the given ID.
   *
   * @param entityClass The class of the entity to find.
   * @param id          The ID of the entity to find.
   * @param <T>         The type of the entity.
   *
   * @return The entity if found, otherwise null.
   */
  <T> T find(Class<T> entityClass, Object id);

  /**
   * Persists the given entity to the database.
   *
   * @param entity The entity to persist.
   * @param <T>    The type of the entity.
   */
  <T> void persist(T entity);

  /**
   * Merges the state of the given entity into the current persistence context.
   *
   * @param entity The entity to merge.
   * @param <T>    The type of the entity.
   *
   * @return The merged entity.
   */
  <T> T merge(T entity);

  /**
   * Detaches the given entity from the persistence context.
   *
   * @param entity The entity to detach.
   */
  void detach(Object entity);

  /**
   * Deletes the given entity from the database.
   *
   * @param entity The entity to delete.
   */
  void delete(Object entity);

  /**
   * Flushes the persistence context to the database.
   */
  void flush();

  /**
   * Closes the session, releasing any database resources.
   * This method is inherited from `AutoCloseable`.
   */
  void close();

}