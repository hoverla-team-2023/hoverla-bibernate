package com.bibernate.hoverla.session;

import java.sql.Connection;
import java.util.Optional;

import org.apache.commons.lang3.NotImplementedException;

import com.bibernate.hoverla.action.DeleteAction;
import com.bibernate.hoverla.action.UpdateAction;
import com.bibernate.hoverla.exceptions.BibernateException;
import com.bibernate.hoverla.query.Query;
import com.bibernate.hoverla.query.QueryImpl;
import com.bibernate.hoverla.session.cache.EntityEntry;
import com.bibernate.hoverla.session.cache.EntityKey;
import com.bibernate.hoverla.session.cache.EntityState;
import com.bibernate.hoverla.session.transaction.Transaction;
import com.bibernate.hoverla.session.transaction.TransactionImpl;
import com.bibernate.hoverla.utils.EntityProxyUtils;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SessionImpl extends AbstractSession implements Session, SessionImplementor {

  private Transaction currentTransaction;

  private boolean isClosed = true;

  @SneakyThrows //todo use properly connection
  public SessionImpl(SessionFactoryImplementor sessionFactoryImplementor) {
    super(sessionFactoryImplementor);
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


  //todo implement clear session caches
  @Override
  public void invalidateCaches() {

  }

  @Override
  public Connection getConnection() {
    return currentConnection;
  }

  @Override
  public Transaction getTransaction() {
    if (currentTransaction != null && currentTransaction.isActive()) {
      return currentTransaction;
    }
    this.currentTransaction = new TransactionImpl(this);
    return currentTransaction;
  }

  private <T> void ensureEntityClassIsRegistered(Class<T> entityClass) {
    if (validateEntityClass(entityClass)) {
      throw new BibernateException("""
                                     The specified class %s is not registered as an entity.
                                     Ensure that the class has been added in configuration and marked as an entity.
                                     """
                                     .formatted(entityClass));
    }
  }

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
