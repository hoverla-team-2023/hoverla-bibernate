package com.bibernate.hoverla.session;

import java.sql.Connection;
import java.util.Optional;

import org.apache.commons.lang3.NotImplementedException;

import com.bibernate.hoverla.action.ActionQueue;
import com.bibernate.hoverla.action.DeleteAction;
import com.bibernate.hoverla.action.UpdateAction;
import com.bibernate.hoverla.exceptions.BibernateException;
import com.bibernate.hoverla.jdbc.JdbcExecutor;
import com.bibernate.hoverla.jdbc.JdbcExecutorImpl;
import com.bibernate.hoverla.metamodel.EntityMapping;
import com.bibernate.hoverla.metamodel.FieldMapping;
import com.bibernate.hoverla.query.Query;
import com.bibernate.hoverla.query.QueryImpl;
import com.bibernate.hoverla.session.cache.EntityEntry;
import com.bibernate.hoverla.session.cache.EntityKey;
import com.bibernate.hoverla.session.cache.EntityState;
import com.bibernate.hoverla.session.dirtycheck.EntityEntryUpdateStateVerifierImpl;
import com.bibernate.hoverla.utils.EntityProxyUtils;
import com.bibernate.hoverla.utils.EntityUtils;
import com.bibernate.hoverla.utils.proxy.BibernateByteBuddyProxyInterceptor;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import static com.bibernate.hoverla.utils.EntityProxyUtils.getProxyInterceptor;

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

  @Getter
  private final EntityRowMapper entityRowMapper;

  @Getter
  private Connection currentConnection;

  @SneakyThrows //todo use properly connection
  public SessionImpl(SessionFactoryImplementor sessionFactoryImplementor) {
    this.persistenceContext = new PersistenceContext(this, new EntityEntryUpdateStateVerifierImpl(this));
    this.sessionFactory = sessionFactoryImplementor;
    this.entityDaoService = new EntityDaoService(this);
    this.actionQueue = new ActionQueue();
    this.currentConnection = sessionFactoryImplementor.getDataSource().getConnection();
    this.jdbcExecutor = new JdbcExecutorImpl(currentConnection);
    this.entityRowMapper = new EntityRowMapper(this);
  }

  @Override
  public <T> T find(Class<T> entityClass, Object id) {
    ensureEntityClassIsRegistered(entityClass);

    EntityKey<T> entityKey = new EntityKey<>(entityClass, id);
    log.info("Finding entity with entity key: {}", entityKey);

    return Optional.ofNullable(persistenceContext.manageEntity(entityKey, () -> entityDaoService.load(entityKey),
                                                               entityEntry -> {}))
      .map(EntityEntry::getEntity)
      .map(entityClass::cast)
      .orElse(null);
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
  public <T> T getReference(Class<T> entityClass, Object id) {
    getEntityMapping(entityClass);
    EntityKey<T> entityKey = new EntityKey<>(entityClass, id);

    return Optional.ofNullable(persistenceContext.manageEntity(entityKey, () -> EntityProxyUtils.createProxy(this, entityKey),
                                                               entityEntry -> {}))
      .map(EntityEntry::getEntity)
      .map(entityClass::cast)
      .orElse(null);
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
  public void remove(Object entity) {
    EntityDetails entityDetails = getEntityDetails(entity);

    EntityEntry entityEntry = persistenceContext.getEntityEntry(entityDetails.entityKey());

    boolean isManaged = (entityEntry != null && entityEntry.getEntityState() == EntityState.MANAGED);

    if (!isManaged && !entityDetails.isProxy()) {
      throw new BibernateException("Removing detached entity: " + entityDetails.entityKey().toString());
    }

    if (isManaged) {
      entityEntry.setEntityState(EntityState.REMOVED);
    }

    actionQueue.addAction(new DeleteAction(entity, entityDaoService));
  }

  @Override
  public void flush() {
    updateEntitiesIfDirty();
    actionQueue.executeActions();
  }

  @Override
  @SneakyThrows
  public void close() {
    //todo implement it properly
    if (currentConnection != null) {
      currentConnection.close();
    }
  }

  //todo think about abstract class where to put this method
  @Override
  public <T> EntityDetails getEntityDetails(T entity) {
    BibernateByteBuddyProxyInterceptor<T> proxyInterceptor = getProxyInterceptor(entity);
    boolean isProxy = proxyInterceptor != null;
    Class<?> entityClass = isProxy ? proxyInterceptor.getEntityClass() : entity.getClass();
    EntityMapping entityMapping = getEntityMapping(entityClass);
    FieldMapping<?> primaryKeyMapping = entityMapping.getPrimaryKeyMapping();
    EntityKey<T> entityKey = isProxy ? new EntityKey<>(proxyInterceptor.getEntityClass(), proxyInterceptor.getEntityId())
                                     : EntityUtils.getEntityKey((Class<T>) entity.getClass(), entity, primaryKeyMapping.getFieldName());

    return new EntityDetails(entityMapping, entityKey, isProxy);
  }

  @Override
  public <T> EntityMapping getEntityMapping(Class<T> entityClass) {
    EntityMapping entityMapping = getSessionFactory()
      .getMetamodel()
      .getEntityMappingMap()
      .get(entityClass);

    if (entityMapping == null) {
      throw new BibernateException("""
                                     The specified class %s is not registered as an entity.
                                     Ensure that the class has been added in configuration and marked as an entity.
                                     """
                                     .formatted(entityClass));
    }

    return entityMapping;
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

  private void updateEntitiesIfDirty() {
    Object[] dirtyEntities = persistenceContext.getUpdatedEntities();
    for (Object entity : dirtyEntities) {
      log.debug("Updating the dirty entity: {}", getEntityDetails(entity).entityKey());
      actionQueue.addAction(new UpdateAction(entity, entityDaoService));
    }
  }

}
