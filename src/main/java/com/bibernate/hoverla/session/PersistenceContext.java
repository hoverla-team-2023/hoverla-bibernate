package com.bibernate.hoverla.session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bibernate.hoverla.session.cache.EntityKey;

import lombok.extern.slf4j.Slf4j;

/**
 * The `PersistenceContext` class is responsible for managing entities in the persistence context.
 * It keeps track of entities that are currently managed by the session and their snapshots for
 * change detection.
 */
@Slf4j
public class PersistenceContext {

  /**
   * A map that holds entities by their keys.
   */
  private final Map<EntityKey<?>, Object> entitiesByKey = new HashMap<>();

  /**
   * A map that holds snapshots of entities by their keys.
   */
  private final Map<EntityKey<?>, Object[]> entitiesSnapshotByKey = new HashMap<>();

  /**
   * Retrieves an entity from the persistence context by its key.
   *
   * @param key The key of the entity.
   * @param <T> The type of the entity.
   *
   * @return The entity associated with the key, cast to the correct type.
   */
  public <T> T getEntity(EntityKey<T> key) {
    log.trace("Getting entity from the context by key {}", key);
    Object entity = entitiesByKey.get(key);
    return key.entityType().cast(entity);
  }

  /**
   * Retrieves a list of dirty entities, which are those that have changed since they were
   * added to the persistence context or since the last time the context was cleared.
   *
   * @return A list of dirty entities.
   */
  public List<?> getDirtyEntities() {
    log.trace("Looking for dirty entities (the ones that have changed)");
    var list = new ArrayList<>();
    return list;
  }

  /**
   * Clears the persistence context, removing all entities and their snapshots.
   */
  public void clear() {
    log.trace("Clearing persistence context");
    entitiesByKey.clear();
    entitiesSnapshotByKey.clear();
  }

}