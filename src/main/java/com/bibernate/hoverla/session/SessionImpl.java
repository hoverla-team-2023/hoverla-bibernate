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
 * Implementation of the {@link Session} interface. This class is responsible for managing
 * database sessions, including querying, persisting, merging, and removing entities, as well
 * as flushing changes to the database and closing the session.
 */
@Slf4j
public class SessionImpl extends AbstractSession implements Session, SessionImplementor {

  private Transaction currentTransaction;

  private boolean isClosed = false;

  public SessionImpl(SessionFactoryImplementor sessionFactoryImplementor) {
    super(sessionFactoryImplementor);
  }
  /**
   * Finds an entity of the specified class and primary key.
   *
   * @param <T>         The type of the entity.
   * @param entityClass The class of the entity.
   * @param id          The primary key of the entity.
   * @return The entity if found, or null if not found.
   * @throws IllegalStateException If the session is not open.
   */
  @Override
  public <T> T find(Class<T> entityClass, Object id) {
    checkIfOpenSession();
    ensureEntityClassIsRegistered(entityClass);

    EntityKey<T> entityKey = new EntityKey<>(entityClass, id);
    return find(entityKey);
  }
  /**
   * Creates a new query for the specified criteria and entity class.
   *
   * @param <T>          The type of the entity.
   * @param criteria     The criteria for the query.
   * @param entityClass  The class of the entity.
   * @return The created Query object.
   * @throws IllegalStateException If the session is not open.
   */
  @Override
  public <T> Query<T> createQuery(String criteria, Class<T> entityClass) {
    checkIfOpenSession();
    ensureEntityClassIsRegistered(entityClass);
    return new QueryImpl<>(this, criteria, entityClass);
  }
  /**
   * Persists a new entity to the database.
   *
   * @param <T>    The type of the entity.
   * @param entity The entity to persist.
   * @throws IllegalStateException If the session is not open.
   * @throws BibernateException    If the entity is a proxy or if the unsaved value strategy is violated.
   */
  @Override
  public <T> void persist(T entity) {
    checkIfOpenSession();
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

    EntityDetails<?> entityDetails = getEntityDetails(entity);
    persistenceContext.manageEntity(entityDetails.entityKey(), () -> entity,
                                    entityEntry -> {});
  }
  /**
   * Gets a reference to an entity without initializing it.
   *
   * @param <T>         The type of the entity.
   * @param entityClass The class of the entity.
   * @param id          The primary key of the entity.
   * @return A proxy representing the entity if found, or null if not found.
   * @throws IllegalStateException If the session is not open.
   */
  @Override
  public <T> T getReference(Class<T> entityClass, Object id) {
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
   * Merges a detached entity back into the persistence context.
   *
   * @param <T>    The type of the entity.
   * @param entity The detached entity to merge.
   * @return The managed entity.
   * @throws IllegalStateException If the session is not open.
   * @throws BibernateException    If the entity cannot be merged.
   */
  @Override
  public <T> T merge(T entity) {
    checkIfOpenSession();
    T detachedEntity = EntityProxyUtils.unProxyAndInitialize(entity);

    EntityDetails<T> entityDetails = getEntityDetails(entity);

    T managedEntity = find(entityDetails.entityKey());

    if (managedEntity == null) {
      throw new BibernateException("Failed to merge entity %s, use persist instead: ".formatted(entityDetails.entityKey()));
    }

    updateFields(managedEntity, entityDetails, detachedEntity);

    return managedEntity;
  }
  /**
   * Detaches an entity from the persistence context.
   *
   * @param entity The entity to detach.
   * @throws IllegalStateException If the session is not open.
   */
  @Override
  public void detach(Object entity) {
    checkIfOpenSession();

    EntityDetails<?> entityDetails = getEntityDetails(entity);
    persistenceContext.removeEntity(entityDetails.entityKey());
  }
  /**
   * Removes an entity from the persistence context and marks it for deletion.
   *
   * @param entity The entity to remove.
   * @throws IllegalStateException If the session is not open.
   * @throws BibernateException    If the entity is not managed or if it is a detached entity.
   */
  @Override
  public void remove(Object entity) {
    checkIfOpenSession();
    EntityDetails<?> entityDetails = getEntityDetails(entity);

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
   * Flushes all pending changes to the database.
   *
   * @throws IllegalStateException If the session is not open.
   */
  @Override
  public void flush() {
    checkIfOpenSession();
    updateEntitiesIfDirty();
    actionQueue.executeActions();
  }
  /**
   * Closes the session, invalidating all caches and closing the database connection.
   * After closing, the session cannot be used anymore.
   *
   * @throws IllegalStateException If the session is not open.
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
    checkIfOpenSession();
    this.persistenceContext.invalidateCache();
  }

  @Override
  public Connection getConnection() {
    checkIfOpenSession();
    return currentConnection;
  }

  @Override
  public Transaction getTransaction() {
    checkIfOpenSession();
    if (currentTransaction != null && currentTransaction.isActive()) {
      return currentTransaction;
    }
    this.currentTransaction = new TransactionImpl(this);
    return currentTransaction;
  }

  private <T> T find(EntityKey<T> entityKey) {
    log.info("Finding entity with entity key: {}", entityKey);

    return Optional.ofNullable(persistenceContext.manageEntity(entityKey, () -> entityDaoService.load(entityKey),
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
    List<Object> dirtyEntities = dirtyCheckService.findDirtyEntities();
    for (Object entity : dirtyEntities) {
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
