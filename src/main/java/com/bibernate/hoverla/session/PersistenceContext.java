package com.bibernate.hoverla.session;

import java.util.HashMap;
import java.util.Map;

import com.bibernate.hoverla.exceptions.BibernateException;
import com.bibernate.hoverla.session.cache.EntityEntry;
import com.bibernate.hoverla.session.cache.EntityKey;
import com.bibernate.hoverla.session.cache.EntityState;

public class PersistenceContext {

  private final Map<EntityKey, EntityEntry> entityKeyEntityEntryMap = new HashMap<>();

  public EntityEntry getEntityEntry(EntityKey entityKey) {
    return entityKeyEntityEntryMap.get(entityKey);
  }

  public void addEntityEntry(EntityKey entityKey, EntityEntry entityEntry) {
    entityKeyEntityEntryMap.put(entityKey, entityEntry);
  }

  public Object putEntityIfAbsent(EntityKey entityKey, Object entity) {
    if (!entityKey.entityType().isAssignableFrom(entity.getClass())) {
      throw new BibernateException();
    }
    return entityKeyEntityEntryMap.computeIfAbsent(entityKey, key ->
        EntityEntry.builder()
          .entityState(EntityState.MANAGED)
          .entity(entity)
          .loadedEntity(null) // make a snapshot
          .lockMode(LockMode.NONE)
          .isReadOnly(false)
          .build())
      .getEntity();
  }

}