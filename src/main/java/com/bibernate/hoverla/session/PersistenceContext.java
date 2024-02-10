package com.bibernate.hoverla.session;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.bibernate.hoverla.session.cache.EntityEntry;
import com.bibernate.hoverla.session.cache.EntityKey;
import com.bibernate.hoverla.session.cache.EntityState;
import com.bibernate.hoverla.session.dirtycheck.DirtyFieldMapping;
import com.bibernate.hoverla.session.dirtycheck.EntityEntryUpdateStateVerifier;
import com.bibernate.hoverla.utils.EntityProxyUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * First-level cache
 */
@Slf4j
@RequiredArgsConstructor
public class PersistenceContext {

  private final Map<EntityKey<?>, EntityEntry> entityKeyEntityEntryMap = new HashMap<>();
  private final SessionImplementor sessionImplementor;
  private final EntityEntryUpdateStateVerifier entityEntryStateVerifier;

  public EntityEntry getEntityEntry(EntityKey<?> entityKey) {
    return entityKeyEntityEntryMap.get(entityKey);
  }

  public EntityEntry manageEntity(EntityKey<?> entityKey, Supplier<Object> getEntityOrProxyFunction, Consumer<EntityEntry> processFunction) {

    EntityEntry entityEntry = entityKeyEntityEntryMap.get(entityKey);
    if (entityEntry == null) {
      entityEntry = putNewEntityEntry(entityKey, getEntityOrProxyFunction);
    } else {
      initialyProxyIfNeeded(getEntityOrProxyFunction, entityEntry);
      entityEntry.setSnapshot(entityEntryStateVerifier.getSnapshot(sessionImplementor.getEntityMapping(entityKey.entityType()), entityEntry.getEntity()));

    }

    processFunction.accept(entityEntry);

    return entityEntry;
  }

  private EntityEntry putNewEntityEntry(EntityKey<?> entityKey, Supplier<Object> getEntityOrProxyFunction) {
    EntityEntry entityEntry = EntityEntry.builder()
      .entityState(EntityState.MANAGED)
      .lockMode(LockMode.NONE)
      .isReadOnly(false)
      .build();
    entityKeyEntityEntryMap.put(entityKey, entityEntry);
    Object entity = getEntityOrProxyFunction.get();

    entityEntry.setEntity(entity);
    entityEntry.setSnapshot(entityEntryStateVerifier.getSnapshot(sessionImplementor.getEntityMapping(entityKey.entityType()), entity));
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

  //todo Yevhenii Savonenko: should return list move it to DirtyCheckService
  public Object[] getUpdatedEntities() {
    return entityEntryStateVerifier.findDirtyEntities(entityKeyEntityEntryMap);
  }

  //todo Yevhenii Savonenko: move it to DirtyCheckService
  public <T> DirtyFieldMapping<?>[] getUpdatedFields(T entity) {
    var entityDetails = sessionImplementor.getEntityDetails(entity);
    var entityEntry = getEntityEntry(entityDetails.entityKey());

    return entityEntryStateVerifier.getUpdatedFields(entityEntry);
  }

}
