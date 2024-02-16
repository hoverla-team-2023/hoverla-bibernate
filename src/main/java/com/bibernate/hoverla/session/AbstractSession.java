package com.bibernate.hoverla.session;

import java.sql.Connection;

import com.bibernate.hoverla.action.ActionQueue;
import com.bibernate.hoverla.jdbc.JdbcExecutor;
import com.bibernate.hoverla.jdbc.JdbcExecutorImpl;
import com.bibernate.hoverla.session.cache.PersistenceContext;
import com.bibernate.hoverla.session.dirtycheck.DirtyCheckService;
import com.bibernate.hoverla.session.dirtycheck.DirtyCheckServiceImpl;

import lombok.Getter;
import lombok.SneakyThrows;

/**
 * Abstract base class implementing common functionality for {@link Session} and {@link SessionImplementor}.
 */
@Getter
public abstract class AbstractSession implements Session, SessionImplementor {

  protected final SessionFactoryImplementor sessionFactory;

  protected final PersistenceContext persistenceContext;

  protected final ActionQueue actionQueue;

  protected final EntityDaoService entityDaoService;

  protected final JdbcExecutor jdbcExecutor;

  protected final EntityRowMapper entityRowMapper;

  protected Connection currentConnection;

  protected final DirtyCheckService dirtyCheckService;

  @SneakyThrows
  public AbstractSession(SessionFactoryImplementor sessionFactory) {
    this.dirtyCheckService = new DirtyCheckServiceImpl(this);
    this.persistenceContext = new PersistenceContext(dirtyCheckService);
    this.sessionFactory = sessionFactory;
    this.entityDaoService = new EntityDaoService(this);
    this.actionQueue = new ActionQueue();
    this.currentConnection = sessionFactory.getDataSource().getConnection();
    this.entityRowMapper = new EntityRowMapper(this);
    this.jdbcExecutor = new JdbcExecutorImpl(this);
  }

}
