package com.bibernate.hoverla.session;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.lang3.NotImplementedException;

import com.bibernate.hoverla.action.ActionQueue;
import com.bibernate.hoverla.jdbc.JdbcExecutor;
import com.bibernate.hoverla.jdbc.JdbcExecutorImpl;
import com.bibernate.hoverla.session.transaction.Transaction;
import com.bibernate.hoverla.session.transaction.TransactionImpl;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SessionImpl implements Session, SessionImplementor {

  @Getter
  private final SessionFactoryImplementor sessionFactory;

  @Getter
  private final PersistenceContext persistenceContext;

  @Getter
  private final ActionQueue actionQueue;

  @Getter
  private final EntityDaoService entityDaoService;

  @Getter
  private final JdbcExecutor jdbcExecutor;

  private Transaction сurrentTransaction;
  private boolean isClosed = true;


  @SneakyThrows //todo use properly connection
  public SessionImpl(SessionFactoryImplementor sessionFactoryImplementor) {
    this.persistenceContext = new PersistenceContext();
    this.sessionFactory = sessionFactoryImplementor;
    this.actionQueue = new ActionQueue();
    this.jdbcExecutor = new JdbcExecutorImpl(sessionFactoryImplementor.getDataSource().getConnection());
    this.entityDaoService = null;
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

  //todo implement clear session caches
  @Override
  public void invalidateCaches() {

  }

  @Override
  public Connection getConnection() throws SQLException {
    return this.sessionFactory.getDataSource().getConnection();
  }

  @Override
  public Transaction getTransaction() {
    if (сurrentTransaction != null && сurrentTransaction.isActive()) {
      log.debug("getting exi");
      return сurrentTransaction;
    }
    return new TransactionImpl(this);
  }
}
