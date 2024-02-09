package com.bibernate.hoverla.session;

import com.bibernate.hoverla.action.ActionQueue;
import com.bibernate.hoverla.jdbc.JdbcExecutor;
import com.bibernate.hoverla.metamodel.EntityMapping;

/**
 * An internal contract that extends the Session interface with additional methods for internal framework usage.
 *
 * <p>This interface extends the Session interface to provide additional methods for accessing the session's
 * associated SessionFactory, PersistenceContext, ActionQueue, and JdbcExecutor. It is intended for internal
 * use within the framework and should not be used directly by external developers.</p>
 */
public interface SessionImplementor extends Session {

  /**
   * Retrieves the SessionFactoryImplementor associated with this session.
   *
   * @return The SessionFactoryImplementor used by this session.
   */
  SessionFactoryImplementor getSessionFactory();

  /**
   * Retrieves the first-level cache (PersistenceContext) used by this session.
   *
   * @return The first-level cache (PersistenceContext) associated with this session.
   */
  PersistenceContext getPersistenceContext();

  /**
   * Retrieves the ActionQueue associated with this session.
   *
   * @return The ActionQueue used by this session for managing EntityActions.
   */
  ActionQueue getActionQueue();

  /**
   * Retrieves the JdbcExecutor associated with this session.
   *
   * @return The JdbcExecutor used by this session for executing JDBC queries.
   */
  JdbcExecutor getJdbcExecutor();

  /**
   * Retrieves the EntityDaoService associated with this session.
   *
   * <p>The EntityDaoService is responsible for managing the persistence and operations of entity objects within the session.
   * It provides essential functionality for creating, updating, and removing entities in the data store.</p>
   *
   * @return The EntityDaoService used by this session for managing entity objects.
   */
  EntityDaoService getEntityDaoService();

  <T> EntityMapping getEntityMapping(Class<T> entity);

  <T> EntityDetails getEntityDetails(T entity);

}
