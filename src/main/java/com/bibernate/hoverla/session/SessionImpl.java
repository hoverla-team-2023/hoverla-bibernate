package com.bibernate.hoverla.session;

import org.apache.commons.lang3.NotImplementedException;

public class SessionImpl implements Session {

  private final SessionFactoryImplementor sessionFactory;
  private final PersistenceContext persistenceContext;

  public SessionImpl(SessionFactoryImplementor sessionFactoryImplementor) {
    persistenceContext = new PersistenceContext();
    this.sessionFactory = sessionFactoryImplementor;
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
    throw new NotImplementedException();
  }

}
