package com.bibernate.hoverla.session.impl;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import com.bibernate.hoverla.session.HoverlaEntityPerseverance;
import com.bibernate.hoverla.session.PersistenceContext;
import com.bibernate.hoverla.session.Session;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * The `SessionImpl` class is an implementation of the `Session` interface.
 * It is responsible for managing the persistence context and the lifecycle of entities.
 */
@Slf4j
@RequiredArgsConstructor
public class SessionImpl implements Session {

  private HoverlaEntityPerseverance perseverance;
  private PersistenceContext persistenceContext = new PersistenceContext();
  private Connection connection;
  private boolean isClosed = true;

  /**
   * Constructs a `SessionImpl` with the specified data source.
   *
   * @param dataSource The data source that provides the database connection.
   */
  public SessionImpl(DataSource dataSource) {
    perseverance = new HoverlaEntityPerseverance(dataSource, this, persistenceContext);
  }

  /**
   * Finds an entity of the specified class with the given ID.
   *
   * @param entityClass The class of the entity to find.
   * @param id          The ID of the entity to find.
   * @param <T>         The type of the entity.
   *
   * @return The entity if found, otherwise null.
   */
  @Override
  public <T> T find(Class<T> entityClass, Object id) {
    checkIfClosed();
    return null;
  }

  /**
   * Persists the given entity to the database.
   *
   * @param entity The entity to persist.
   * @param <T>    The type of the entity.
   */
  @Override
  public <T> void persist(T entity) {
    checkIfClosed();
    log.info("Persisting entity {}", entity);
  }

  /**
   * Merges the state of the given entity into the current persistence context.
   *
   * @param entity The entity to merge.
   * @param <T>    The type of the entity.
   *
   * @return The merged entity.
   */
  @Override
  public <T> T merge(T entity) {
    checkIfClosed();
    // Implementation of merge method
    return null;
  }

  /**
   * Detaches the given entity from the persistence context.
   *
   * @param entity The entity to detach.
   */
  @Override
  public void detach(Object entity) {
    checkIfClosed();
    // Implementation of detach method
  }

  /**
   * Deletes the given entity from the database.
   *
   * @param entity The entity to delete.
   */
  @Override
  public void delete(Object entity) {
    checkIfClosed();
    // Implementation of delete method
  }

  /**
   * Flushes the persistence context to the database.
   */
  @Override
  public void flush() {
    checkIfClosed();
    // Implementation of flush method
  }

  /**
   * Checks if the session is closed and throws an exception if it is.
   *
   * @throws IllegalStateException if the session is closed.
   */
  private void checkIfClosed() {
    if (this.connection == null || isClosed) {
      throw new IllegalStateException("Session is closed");
    }
  }

  /**
   * Closes the session, releasing any database resources.
   */
  @Override
  public void close() {
    if (!isClosed) {
      try {
        if (connection != null && !connection.isClosed()) {
          connection.close();
        }
      } catch (SQLException e) {
        e.printStackTrace();
      }
      isClosed = true;
    }
  }

}