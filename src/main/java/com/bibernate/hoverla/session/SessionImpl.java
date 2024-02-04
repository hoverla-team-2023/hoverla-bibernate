package com.bibernate.hoverla.session;

import org.apache.commons.lang3.NotImplementedException;

import com.bibernate.hoverla.action.ActionQueue;
import com.bibernate.hoverla.exceptions.BibernateException;
import com.bibernate.hoverla.jdbc.JdbcExecutor;
import com.bibernate.hoverla.jdbc.JdbcExecutorImpl;
import com.bibernate.hoverla.query.Query;
import com.bibernate.hoverla.query.QueryImpl;

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
  private final EntityDaoService entityDaoService;

  @Getter
  private final JdbcExecutor jdbcExecutor;

  @SneakyThrows //todo use properly connection
  public SessionImpl(SessionFactoryImplementor sessionFactoryImplementor) {
    this.persistenceContext = new PersistenceContext();
    this.sessionFactory = sessionFactoryImplementor;
    this.entityDaoService = new EntityDaoService();
    this.actionQueue = new ActionQueue();
    this.jdbcExecutor = new JdbcExecutorImpl(sessionFactoryImplementor.getDataSource().getConnection());
  }

  @Override
  public <T> T find(Class<T> entityClass, Object id) {
    throw new NotImplementedException();
  }

  @Override
  public <T> Query<T> createQuery(String criteria, Class<T> entityClass) {
    ensureEntityClassIsRegistered(entityClass);
    return new QueryImpl<>(this, criteria, entityClass);
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

  /**
   * Ensures that the provided entity class is registered with the session factory.
   * If the entity class is not registered, a {@link BibernateException} is thrown with an error message.
   *
   * @param <T>         type of the entity class.
   * @param entityClass The entity class to validate.
   *
   * @throws BibernateException If the entity class is not registered with the session factory.
   */
  private <T> void ensureEntityClassIsRegistered(Class<T> entityClass) {
    if (validateEntityClass(entityClass)) {
      throw new BibernateException("""
                                     The specified class %s is not registered as an entity.
                                     Ensure that the class has been added in configuration and marked as an entity.
                                     """
                                     .formatted(entityClass));
    }
  }

  /**
   * Validates whether the provided entity class is registered with the session factory.
   *
   * @param <T>         type of the entity class.
   * @param entityClass The entity class to validate.
   *
   * @return {@code true} if the entity class is not registered, {@code false} otherwise.
   */
  private <T> boolean validateEntityClass(Class<T> entityClass) {
    return !sessionFactory.getMetamodel().getEntityMappingMap().containsKey(entityClass);
  }

}
