package com.bibernate.hoverla.session;

import com.bibernate.hoverla.action.ActionQueue;
import com.bibernate.hoverla.jdbc.JdbcExecutor;

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
   * Retrieves the PersistenceContext associated with this session.
   *
   * @return The PersistenceContext used by this session for managing entity state.
   */
  PersistenceContext getPersistenceContext();

  /**
   * Retrieves the ActionQueue associated with this session.
   *
   * @return The ActionQueue used by this session for queuing and executing actions.
   */
  ActionQueue getActionQueue();

  /**
   * Retrieves the JdbcExecutor associated with this session.
   *
   * @return The JdbcExecutor used by this session for executing JDBC queries.
   */
  JdbcExecutor getJdbcExecutor();

  EntityPersister getEntityPersister();

}
