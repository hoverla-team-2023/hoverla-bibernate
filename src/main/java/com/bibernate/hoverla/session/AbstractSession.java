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

public abstract class AbstractSession implements Session, SessionImplementor {

  @Getter
  protected final SessionFactoryImplementor sessionFactory;

  @Getter
  protected final PersistenceContext persistenceContext;

  @Getter
  protected final ActionQueue actionQueue;

  @Getter
  protected final EntityDaoService entityDaoService;

  @Getter
  protected final JdbcExecutor jdbcExecutor;

  @Getter
  protected final EntityRowMapper entityRowMapper;

  @Getter
  protected Connection currentConnection;

  @Getter
  protected final DirtyCheckService dirtyCheckService;

  @SneakyThrows
  public AbstractSession(SessionFactoryImplementor sessionFactory) {
    this.dirtyCheckService = new DirtyCheckServiceImpl(this);
    this.persistenceContext = new PersistenceContext(this, dirtyCheckService);
    this.sessionFactory = sessionFactory;
    this.entityDaoService = new EntityDaoService(this);
    this.actionQueue = new ActionQueue();
    this.currentConnection = sessionFactory.getDataSource().getConnection();
    this.entityRowMapper = new EntityRowMapper(this);
    this.jdbcExecutor = new JdbcExecutorImpl(this);
  }

}
