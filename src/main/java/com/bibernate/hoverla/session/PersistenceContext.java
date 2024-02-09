package com.bibernate.hoverla.session;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import com.bibernate.hoverla.session.cache.EntityEntry;
import com.bibernate.hoverla.session.cache.EntityKey;
import com.bibernate.hoverla.session.dirtycheck.DirtyFieldMapping;
import com.bibernate.hoverla.session.dirtycheck.EntityEntryUpdateStateVerifier;

import lombok.RequiredArgsConstructor;

/**
 * First-level cache
 */
@RequiredArgsConstructor
public class PersistenceContext {

  private final Map<EntityKey, EntityEntry> entityKeyEntityEntryMap = new HashMap<>();
  private final SessionImplementor sessionImplementor;
  private final EntityEntryUpdateStateVerifier entityEntryStateVerifier;

  public EntityEntry getEntityEntry(EntityKey entityKey) {
    return entityKeyEntityEntryMap.get(entityKey);
  }

  public void putEntityEntry(EntityKey entityKey, EntityEntry entityEntry) {
    entityKeyEntityEntryMap.put(entityKey, entityEntry);
  }

  public EntityEntry compute(EntityKey entityKey, BiFunction<? super EntityKey, ? super EntityEntry, ? extends EntityEntry> remappingFunction) {
    return entityKeyEntityEntryMap.compute(entityKey, remappingFunction);
  }

  public Object[] getUpdatedEntities() {
    return entityEntryStateVerifier.findDirtyEntities(entityKeyEntityEntryMap);
  }

  public <T> DirtyFieldMapping<?>[] getUpdatedFields(T entity) {
    var entityDetails = sessionImplementor.getEntityDetails(entity);
    var entityEntry = getEntityEntry(entityDetails.entityKey());

    return entityEntryStateVerifier.getUpdatedFields(entityEntry);
  }

}
