package com.bibernate.hoverla.session;

import org.apache.commons.lang3.NotImplementedException;

import com.bibernate.hoverla.action.ActionQueue;
import com.bibernate.hoverla.jdbc.JdbcExecutor;
import com.bibernate.hoverla.jdbc.JdbcExecutorImpl;

import lombok.Getter;
import lombok.SneakyThrows;

public class SessionImpl implements Session, SessionImplementor {

  @Getter
  private final SessionFactoryImplementor sessionFactory;

  @Getter
  private final PersistenceContext persistenceContext;

  @Getter
  private final ActionQueue actionQueue;

  @Getter
  private final EntityPersister entityPersister;

  @Getter
  private final JdbcExecutor jdbcExecutor;

  @SneakyThrows //todo use properly connection
  public SessionImpl(SessionFactoryImplementor sessionFactoryImplementor) {
    this.persistenceContext = new PersistenceContext();
    this.sessionFactory = sessionFactoryImplementor;
    this.actionQueue = new ActionQueue();
    this.jdbcExecutor = new JdbcExecutorImpl(sessionFactoryImplementor.getDataSource().getConnection());
    entityPersister = null;
  }

  @Override
  public <T> T find(Class<T> entityClass, Object id) {
    throw new NotImplementedException();
  }

  @Override
  public <T> void persist(T entity) {
    throw new NotImplementedException();

  }

  @Override
  public <T> T merge(T entity) {
    throw new NotImplementedException();
  }

  @Override
  public void detach(Object entity) {
    throw new NotImplementedException();
  }

  @Override
  public void delete(Object entity) {
    throw new NotImplementedException();
  }

  @Override
  public void flush() {
    throw new NotImplementedException();
  }

  @Override
  public void close() {
    //todo
  }

}
