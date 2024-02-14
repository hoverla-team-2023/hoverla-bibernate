package com.bibernate.hoverla.session.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.bibernate.hoverla.collection.PersistenceLazyList;
import com.bibernate.hoverla.session.LockMode;
import com.bibernate.hoverla.session.SessionImplementor;
import com.bibernate.hoverla.session.dirtycheck.DirtyCheckService;
import com.bibernate.hoverla.utils.EntityProxyUtils;
import com.bibernate.hoverla.utils.proxy.BibernateByteBuddyProxyInterceptor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * First-level cache
 */
@Slf4j
@RequiredArgsConstructor
public class PersistenceContext {

  @Getter
  private final Map<EntityKey<?>, EntityEntry> entityKeyEntityEntryMap = new HashMap<>();
  private final Map<CollectionKey<?>, PersistenceLazyList<?>> collectionsMap = new HashMap<>();

  private final SessionImplementor sessionImplementor;
  private final DirtyCheckService dirtyCheckService;

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

  public void manageCollection(CollectionKey<?> collectionKey, PersistenceLazyList<?> collection) {
    collectionsMap.put(collectionKey, collection);
  }

  public void invalidateCache() {
    unlinkSession();

    this.collectionsMap.clear();
    this.entityKeyEntityEntryMap.clear();

  }

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
      .lockMode(LockMode.NONE)
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
