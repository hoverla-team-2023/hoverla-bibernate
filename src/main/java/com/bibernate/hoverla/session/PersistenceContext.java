package com.bibernate.hoverla.session;

import java.util.HashMap;
import java.util.Map;

import com.bibernate.hoverla.session.cache.EntityEntry;
import com.bibernate.hoverla.session.cache.EntityKey;

public class PersistenceContext {

  private final Map<EntityKey, EntityEntry> entityKeyEntityEntryMap = new HashMap<>();

  public EntityEntry getEntityEntry(EntityKey entityKey) {
    return entityKeyEntityEntryMap.get(entityKey);
  }

  public void addEntityEntry(EntityKey entityKey, EntityEntry entityEntry) {
    entityKeyEntityEntryMap.put(entityKey, entityEntry);
  }

}