package com.bibernate.hoverla.session;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.NotImplementedException;

import com.bibernate.hoverla.action.DeleteAction;
import com.bibernate.hoverla.action.IdentityInsertAction;
import com.bibernate.hoverla.action.InsertAction;
import com.bibernate.hoverla.action.UpdateAction;
import com.bibernate.hoverla.exceptions.BibernateException;
import com.bibernate.hoverla.exceptions.PersistOperationException;
import com.bibernate.hoverla.generator.Generator;
import com.bibernate.hoverla.metamodel.FieldMapping;
import com.bibernate.hoverla.metamodel.IdGeneratorStrategy;
import com.bibernate.hoverla.metamodel.UnsavedValueStrategy;
import com.bibernate.hoverla.query.Query;
import com.bibernate.hoverla.query.QueryImpl;
import com.bibernate.hoverla.session.cache.EntityEntry;
import com.bibernate.hoverla.session.cache.EntityKey;
import com.bibernate.hoverla.session.cache.EntityState;
import com.bibernate.hoverla.session.transaction.Transaction;
import com.bibernate.hoverla.session.transaction.TransactionImpl;
import com.bibernate.hoverla.utils.EntityProxyUtils;
import com.bibernate.hoverla.utils.EntityUtils;

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
    verifyIsNotProxy(entity);

    var entityMapping = getEntityMapping(entity.getClass());
    var primaryKeyMapping = entityMapping.getPrimaryKeyMapping();

    verifyUnsavedValueStrategy(entity, primaryKeyMapping);

    if (isIdentityGenerated(primaryKeyMapping)) {
      actionQueue.addAction(new IdentityInsertAction(entity, entityDaoService));
    } else {
      populateGeneratedIdIfRequired(entity, primaryKeyMapping);
      actionQueue.addAction(new InsertAction(entity, entityDaoService));
    }

    EntityDetails entityDetails = getEntityDetails(entity);
    persistenceContext.manageEntity(entityDetails.entityKey(), () -> entity,
                                    entityEntry -> {});
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
    invalidateCaches();
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

  private boolean isIdentityGenerated(FieldMapping<?> primaryKeyMapping) {
    return primaryKeyMapping.getIdGeneratorStrategy().isIdentityGenerated();
  }

  private <T> void verifyIsNotProxy(T entity) {
    if (EntityProxyUtils.isProxy(entity)) {
      throw new PersistOperationException("Proxy object passed to persist");
    }
  }

  private <T> void verifyUnsavedValueStrategy(T entity, FieldMapping<?> primaryKeyMapping) {
    IdGeneratorStrategy idGeneratorStrategy = primaryKeyMapping.getIdGeneratorStrategy();
    if (idGeneratorStrategy.getUnsavedValueStrategy() == UnsavedValueStrategy.NULL) {
      Object fieldValue = EntityUtils.getFieldValue(primaryKeyMapping.getFieldName(), entity);
      if (fieldValue != null) {
        throw new PersistOperationException("Detached entity passed to persist: " + entity.getClass().getName());
      }
    }
  }

  private <T> void populateGeneratedIdIfRequired(T entity, FieldMapping<?> primaryKeyMapping) {
    IdGeneratorStrategy idGeneratorStrategy = primaryKeyMapping.getIdGeneratorStrategy();
    Generator generator = idGeneratorStrategy.getGenerator();
    if (generator != null) {
      Object generatedValue = generator.generateNext(this.getConnection());
      EntityUtils.setFieldValue(primaryKeyMapping.getFieldName(), entity, generatedValue);
    }
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

  /**
   * Updates the entities that have been marked as dirty in the persistence context.
   * This method retrieves all entities that have been updated (marked as dirty) from the persistence context.
   * For each dirty entity, it logs the details of the entity and adds an update action to the action queue.
   * The update action is associated with the entity and the entity DAO service.
   */
  private void updateEntitiesIfDirty() {
    List<Object> dirtyEntities = dirtyCheckService.findDirtyEntities();
    for (Object entity : dirtyEntities) {
      log.debug("Updating the dirty entity: {}", getEntityDetails(entity).entityKey());
      actionQueue.addAction(new UpdateAction(entity, entityDaoService));
    }
  }

}
