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

  /**
   * Puts an entity into the persistence context if it is not already present.
   * If the entity key's entity type is not assignable from the entity's class,
   * a {@link BibernateException} is thrown.
   *
   * @param entityKey The key associated with the entity.
   * @param entity    The entity to be put into the persistence context.
   *
   * @return The entity that was put into the persistence context, or the existing entity if one was already present.
   *
   * @throws BibernateException If the entity key's entity type is not assignable from the entity's class.
   */
  public Object putEntityIfAbsent(EntityKey entityKey, Object entity) {
    if (!entityKey.entityType().isAssignableFrom(entity.getClass())) {
      throw new BibernateException("Incompatible entity type. Expected: " + entityKey.entityType() + ", Actual: " + entity.getClass());
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