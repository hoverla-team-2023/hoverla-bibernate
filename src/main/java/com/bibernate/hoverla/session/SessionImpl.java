package com.bibernate.hoverla.session;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import com.bibernate.hoverla.action.DeleteAction;
import com.bibernate.hoverla.action.IdentityInsertAction;
import com.bibernate.hoverla.action.InsertAction;
import com.bibernate.hoverla.action.UpdateAction;
import com.bibernate.hoverla.exceptions.BibernateException;
import com.bibernate.hoverla.exceptions.BibernateSqlException;
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

import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of the Session interface providing session management functionalities.
 */
@Slf4j
public class SessionImpl extends AbstractSession implements Session, SessionImplementor {

  private Transaction currentTransaction;

  private boolean isClosed = false;

  public SessionImpl(SessionFactoryImplementor sessionFactoryImplementor) {
    super(sessionFactoryImplementor);
  }

  /**
   * Finds an entity by its class and identifier.
   *
   * @param entityClass The class of the entity.
   * @param id          The identifier of the entity.
   *
   * @return The found entity.
   *
   * @see Session#find(Class, Object)
   */
  @Override
  public <T> T find(Class<T> entityClass, Object id) {
    return find(entityClass, id, LockMode.NONE);
  }

  /**
   * Finds an entity by its class type and primary key.
   *
   * @param entityClass The class of the entity to find.
   * @param id          The primary key of the entity.
   * @param <T>         The type of the entity.
   *
   * @return The found entity or {@code null} if the entity does not exist.
   */
  @Override
  public <T> T find(Class<T> entityClass, Object id, LockMode lockMode) {
    log.debug("Finding entity of class {} with id {} and lock mode {}", entityClass.getSimpleName(), id, lockMode);

    checkIfOpenSession();
    ensureEntityClassIsRegistered(entityClass);

    EntityKey<T> entityKey = new EntityKey<>(entityClass, id);
    return find(entityKey, lockMode);
  }

  /**
   * Creates a new query instance for the given criteria and entity class.
   *
   * @param criteria    The criteria for the query.
   * @param entityClass The class of the entity.
   *
   * @return A new Query instance.
   *
   * @see Session#createQuery(String, Class)
   */
  @Override
  public <T> Query<T> createQuery(String criteria, Class<T> entityClass) {
    checkIfOpenSession();
    ensureEntityClassIsRegistered(entityClass);
    return new QueryImpl<>(this, criteria, entityClass);
  }

  /**
   * Persists a new entity into the database.
   *
   * @param entity The entity to be persisted.
   * @param <T>    The type of the entity.
   *
   * @see Session#persist(Object)
   */
  @Override
  public <T> void persist(T entity) {
    log.trace("Persisting entity...");

    checkIfOpenSession();
    verifyIsNotProxy(entity);

    var entityMapping = getEntityMapping(entity.getClass());
    var primaryKeyMapping = entityMapping.getPrimaryKeyMapping();

    verifyUnsavedValueStrategy(entity, primaryKeyMapping);

    if (isIdentityGenerated(primaryKeyMapping)) {
      log.debug("Identity generation strategy detected. Adding IdentityInsertAction for entity.");
      actionQueue.addAction(new IdentityInsertAction(entity, entityDaoService));
    } else {
      log.debug("Adding InsertAction for entity.");
      populateGeneratedIdIfRequired(entity, primaryKeyMapping);
      actionQueue.addAction(new InsertAction(entity, entityDaoService));
    }

    EntityDetails<?> entityDetails = getEntityDetails(entity);
    persistenceContext.manageEntity(entityDetails.entityKey(), () -> entity,
                                    entityEntry -> {});

    log.debug("Entity persisted successfully.");

  }

  /**
   * Gets a reference to the entity of the specified class with the given identifier.
   *
   * @param entityClass The class of the entity.
   * @param id          The identifier of the entity.
   *
   * @return A reference to the entity.
   *
   * @see Session#getReference(Class, Object)
   */
  @Override
  public <T> T getReference(Class<T> entityClass, Object id) {
    log.debug("Getting reference for entity class: {} with id: {}", entityClass.getSimpleName(), id);

    checkIfOpenSession();
    getEntityMapping(entityClass);
    EntityKey<T> entityKey = new EntityKey<>(entityClass, id);

    return Optional.ofNullable(persistenceContext.manageEntity(entityKey, () -> EntityProxyUtils.createProxy(this, entityKey),
                                                               entityEntry -> {}))
      .map(EntityEntry::getEntity)
      .map(entityClass::cast)
      .orElse(null);
  }

  /**
   * Merges the state of the given entity into the current persistence context.
   *
   * @param entity The entity to be merged.
   *
   * @return The managed copy of the entity.
   *
   * @throws BibernateException if the entity cannot be merged.
   * @see Session#merge(Object)
   */
  @Override
  public <T> T merge(T entity) {
    log.debug("Merging entity...");

    checkIfOpenSession();
    T detachedEntity = EntityProxyUtils.unProxyAndInitialize(entity);

    EntityDetails<T> entityDetails = getEntityDetails(entity);

    log.trace("Merging entity: {}", entityDetails.entityKey());

    T managedEntity = find(entityDetails.entityKey());

    if (managedEntity == null) {
      throw new BibernateException("Failed to merge entity %s, use persist instead: ".formatted(entityDetails.entityKey()));
    }

    updateFields(managedEntity, entityDetails, detachedEntity);

    log.debug("Entity merged successfully: {}", entityDetails.entityKey());

    return managedEntity;
  }

  /**
   * Detaches the given entity from the persistence context.
   *
   * @param entity The entity to be detached.
   */
  @Override
  public void detach(Object entity) {
    log.trace("Detaching entity...");
    checkIfOpenSession();

    EntityDetails<?> entityDetails = getEntityDetails(entity);
    log.debug("Detaching entity: {}", entityDetails.entityKey());

    persistenceContext.removeEntity(entityDetails.entityKey());
    log.debug("Entity detached: {}", entityDetails.entityKey());
  }

  /**
   * Removes an entity from the database.
   *
   * @param entity The entity to remove.
   */
  @Override
  public void remove(Object entity) {
    checkIfOpenSession();
    EntityDetails<?> entityDetails = getEntityDetails(entity);

    log.debug("Removing entity: {}", entityDetails.entityKey());

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

  /**
   * Flushes the session, synchronizing the in-memory state of managed entities with the database.
   */
  @Override
  public void flush() {
    log.debug("Flushing session.");

    checkIfOpenSession();
    updateEntitiesIfDirty();
    actionQueue.executeActions();

    log.debug("Session flushed successfully.");
  }

  /**
   * Closes the session, releasing all resources associated with it.
   */
  @Override
  public void close() {
    checkIfOpenSession();
    invalidateCaches();
    closeConnection();
    this.isClosed = true;
  }

  /**
   * Invalidates caches associated with the current session.
   */
  @Override
  public void invalidateCaches() {
    log.debug("Invalidating caches associated with the current session.");
    checkIfOpenSession();
    this.persistenceContext.invalidateCache();
  }

  /**
   * Retrieves the connection associated with this session.
   *
   * @return The connection associated with this session.
   */
  @Override
  public Connection getConnection() {
    checkIfOpenSession();
    return currentConnection;
  }

  /**
   * Retrieves the transaction associated with this session.
   *
   * @return The transaction associated with this session.
   */
  @Override
  public Transaction getTransaction() {
    log.debug("Retrieving session transaction.");

    checkIfOpenSession();
    if (currentTransaction != null && currentTransaction.isActive()) {
      log.trace("Session transaction retrieved successfully.");

      return currentTransaction;
    }

    this.currentTransaction = new TransactionImpl(this);

    log.trace("Session transaction retrieved successfully.");
    return currentTransaction;
  }

  private <T> T find(EntityKey<T> entityKey) {
    return find(entityKey, LockMode.NONE);
  }

  private <T> T find(EntityKey<T> entityKey, LockMode lockMode) {
    log.info("Finding entity with entity key: {}", entityKey);

    return Optional.ofNullable(persistenceContext.manageEntity(entityKey, () -> entityDaoService.load(entityKey, lockMode),
                                                               entityEntry -> {}))
      .map(EntityEntry::getEntity)
      .map(entityKey.entityType()::cast)
      .orElse(null);
  }

  private <T> void updateFields(T managedEntity, EntityDetails entityDetails, T detachedEntity) {
    T managedEntityUnProxied = EntityProxyUtils.unProxyAndInitialize(managedEntity);

    for (FieldMapping<?> fieldMapping : entityDetails.entityMapping().getFieldMappings(a -> !a.isOneToMany())) {
      Object fieldValue = EntityUtils.getFieldValue(fieldMapping.getFieldName(), detachedEntity);
      EntityUtils.setFieldValue(fieldMapping.getFieldName(), managedEntityUnProxied, fieldValue);
    }
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

  private void closeConnection() {
    if (currentConnection != null) {
      try {
        currentConnection.close();
      } catch (SQLException exc) {
        throw new BibernateSqlException("Failed to close connections", exc);
      }
    }
  }

  /**
   * Updates the entities that have been marked as dirty in the persistence context.
   * This method retrieves all entities that have been updated (marked as dirty) from the persistence context.
   * For each dirty entity, it logs the details of the entity and adds an update action to the action queue.
   * The update action is associated with the entity and the entity DAO service.
   */
  private void updateEntitiesIfDirty() {
    log.debug("Updating dirty entities.");

    List<?> dirtyEntities = dirtyCheckService.findDirtyEntities();
    for (var entity : dirtyEntities) {
      log.debug("Updating the dirty entity: {}", getEntityDetails(entity).entityKey());
      actionQueue.addAction(new UpdateAction(entity, entityDaoService));
    }
  }

  private void checkIfOpenSession() {
    if (isClosed) {
      throw new BibernateException("Current session is closed");
    }
  }

}
