package com.bibernate.hoverla.session.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.bibernate.hoverla.collection.PersistenceLazyList;
import com.bibernate.hoverla.session.SessionImplementor;
import com.bibernate.hoverla.session.dirtycheck.DirtyCheckService;
import com.bibernate.hoverla.utils.EntityProxyUtils;
import com.bibernate.hoverla.utils.proxy.BibernateByteBuddyProxyInterceptor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Represents the first-level cache for managing entities and collections within a session.
 */
@Slf4j
@RequiredArgsConstructor
public class PersistenceContext {

  private final SessionImplementor sessionImplementor;
  private final DirtyCheckService dirtyCheckService;

  @Getter
  private final Map<EntityKey<?>, EntityEntry> entityKeyEntityEntryMap = new HashMap<>();
  private final Map<CollectionKey<?>, PersistenceLazyList<?>> collectionsMap = new HashMap<>();

  /**
   * Retrieves the entity entry for the specified entity key.
   *
   * @param entityKey the entity key
   *
   * @return the entity entry, or null if not found
   */
  public EntityEntry getEntityEntry(EntityKey<?> entityKey) {
    return entityKeyEntityEntryMap.get(entityKey);
  }

  public EntityEntry manageEntity(EntityKey<?> entityKey, Supplier<Object> getEntityOrProxyFunction, Consumer<EntityEntry> processFunction) {

    EntityEntry entityEntry = entityKeyEntityEntryMap.get(entityKey);
    if (entityEntry == null) {
      entityEntry = putNewEntityEntry(entityKey, getEntityOrProxyFunction);
    } else {
      initialyProxyIfNeeded(getEntityOrProxyFunction, entityEntry);
      entityEntry.setSnapshot(dirtyCheckService.getSnapshot(sessionImplementor.getEntityMapping(entityKey.entityType()), entityEntry.getEntity()));
    }

    if (entityEntry != null) {
      processFunction.accept(entityEntry);
    }

    return entityEntry;
  }

  /**
   * Manages a collection by storing it in the collections map.
   *
   * @param collectionKey The key associated with the collection.
   * @param collection    The collection to be managed.
   */
  public void manageCollection(CollectionKey<?> collectionKey, PersistenceLazyList<?> collection) {
    collectionsMap.put(collectionKey, collection);
  }

  /**
   * Invalidates the cache by unlinking session-related entities and clearing the entity and collection maps.
   */
  public void invalidateCache() {
    unlinkSession();

    this.collectionsMap.clear();
    this.entityKeyEntityEntryMap.clear();

  }

  /**
   * Removes the entity entry associated with the specified entity key from the context.
   *
   * @param entityKey The entity key for which the associated entity entry should be removed.
   */
  public void removeEntity(EntityKey<?> entityKey) {
    entityKeyEntityEntryMap.remove(entityKey);
  }

  /**
   * Checks if the entity associated with the specified entity key is detached from the persistence context.
   *
   * @param entityKey The entity key to check for detachment.
   *
   * @return True if the entity is detached, false otherwise.
   */
  public boolean isDetached(EntityKey<?> entityKey) {
    return !entityKeyEntityEntryMap.containsKey(entityKey);
  }

  /**
   * Unlinks the current session from all entity entries and collections in the persistence context.
   * This method iterates over all entity entries in the entity key entry map and all collections in the collections map,
   * unlinking their sessions by invoking the appropriate methods.
   */
  private void unlinkSession() {
    entityKeyEntityEntryMap.values().stream()
      .map(EntityEntry::getEntity)
      .map(EntityProxyUtils::getProxyInterceptor)
      .filter(Objects::nonNull).forEach(
        BibernateByteBuddyProxyInterceptor::unlinkSession);

    collectionsMap.values()
      .forEach(PersistenceLazyList::unlinkSession);
  }

  private EntityEntry putNewEntityEntry(EntityKey<?> entityKey, Supplier<Object> getEntityOrProxyFunction) {
    EntityEntry entityEntry = EntityEntry.builder()
      .entityState(EntityState.MANAGED)
      .isReadOnly(false)
      .build();
    entityKeyEntityEntryMap.put(entityKey, entityEntry);
    Object entity = getEntityOrProxyFunction.get();

    if (entity == null) {
      entityKeyEntityEntryMap.remove(entityKey);
      return null;
    }

    entityEntry.setEntity(entity);
    entityEntry.setSnapshot(dirtyCheckService.getSnapshot(sessionImplementor.getEntityMapping(entityKey.entityType()), entity));
    return entityEntry;
  }

  private void initialyProxyIfNeeded(Supplier<Object> getEntityOrProxyFunction, EntityEntry entityEntry) {
    if (EntityProxyUtils.isUnitializedProxy(entityEntry.getEntity())) {
      Object entity = getEntityOrProxyFunction.get();
      if (!EntityProxyUtils.isProxy(entity)) {
        EntityProxyUtils.initializeProxy(entityEntry.getEntity(), entity);
      }
    }
  }

}
