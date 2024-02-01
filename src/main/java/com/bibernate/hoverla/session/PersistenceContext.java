package com.bibernate.hoverla.session;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.bibernate.hoverla.session.cache.EntityKey;

import lombok.extern.slf4j.Slf4j;

/**
 * The `PersistenceContext` class represents a context for managing entities.
 * It provides methods for adding, retrieving, and managing entities.
 * Each instance of `PersistenceContext` manages a collection of entities and their snapshots.
 *
 * @param <T> The type of entities managed by this context.
 */
@Slf4j
public class PersistenceContext<T> {
  /**
   * Represents a mapping of entities to their corresponding keys.
   *
   * <p>
   * This map stores entities as values, and their respective keys as keys.
   * </p>
   *
   * <p>
   * The mapping supports null keys and null values.
   * </p>
   *
   * @param <T> the type of the entities
   */
  private final Map<Optional<EntityKey<T>>, T> entitiesByKey = new HashMap<Optional<EntityKey<T>>, T>();
  /**
   * Snapshot of entities stored in a map based on their keys.
   * The keys are of type EntityKey<T> and are wrapped in Optional.
   * The values are arrays of type Object[], containing the entity snapshot data.
   */
  private final Map<Optional<EntityKey<T>>, Object[]> entitiesSnapshotByKey = new HashMap<Optional<EntityKey<T>>, Object[]>();

  /**
   * Manages the entity by checking if it is already present in the context. If the entity is present,
   * it returns the existing entity from the context. If the entity is not present, it adds the new entity
   * to the context.
   *
   * @param entity The entity to be managed.
   * @return The managed entity. If the entity is already present in the context, it returns the existing entity.
   *         If the entity is not present, it adds the entity to the context and returns the added entity.
   */
  public T manageEntity(T entity) {
    log.trace("Checking entity {}", entity);
    Optional<EntityKey<T>> key = EntityKey.valueOf(entity);
    if (entitiesByKey.containsKey(key)) {
      log.trace("Entity is already in the context.");
      return entitiesByKey.get(key);
    } else {
      log.trace("Adding new entity {} to the context", entity);
      return addEntity(entity);
    }
  }

  /**
   * Retrieves the entity associated with the provided key from the context.
   *
   * @param key the key used to identify the entity
   * @return the entity associated with the key, or null if not found
   */
  public T getEntity(EntityKey<T> key) {
    log.trace("Getting entity from the context by key {}", key);
    return entitiesByKey.get(key);
  }

  /**
   * Adds an entity to the PersistenceContext.
   *
   * @param entity the entity to be added
   * @return the added entity
   */
  public T addEntity(T entity) {
    log.trace("Adding entity {} to the PersistenceContext", entity);
    Optional<EntityKey<T>> key = EntityKey.valueOf(entity);
    entitiesByKey.put(key, entity);
    entitiesSnapshotByKey.put(key, createEntitySnapshot(entity));
    return entity;
  }

  /**
   * Checks if the given entity exists in the context.
   *
   * @param entity the entity to check
   * @return true if the entity exists, false otherwise
   */
  public boolean contains(T entity) {
    log.trace("Checking if entity {} exists in the context", entity);
    return entitiesByKey.containsKey(EntityKey.valueOf(entity));
  }

  /**
   * Retrieves the list of dirty entities (the ones that have changed).
   *
   * @return The list of dirty entities.
   */
  public List<T> getDirtyEntities() {
    log.trace("Looking for dirty entities (the ones that have changed)");
    List<T> list = new ArrayList<>();
    for (Map.Entry<Optional<EntityKey<T>>, T> entry : entitiesByKey.entrySet()) {
      var currentEntity = entry.getValue();
      var currentEntitySnapshot = createEntitySnapshot(currentEntity);
      var initialSnapshot = entitiesSnapshotByKey.get(entry.getKey());
      log.trace("Comparing snapshots: {} <=> {}", initialSnapshot, currentEntitySnapshot);
      if (!Arrays.equals(currentEntitySnapshot, initialSnapshot)) {
        log.trace("Found dirty entity {}", currentEntity);
        log.trace("Initial snapshot {}", initialSnapshot);
        list.add(currentEntity);
      }
    }
    return list;
  }

  /**
   * Clears the persistence context.
   * <p>
   * This method clears the entities stored in the persistence context. It removes all entities and
   * their corresponding snapshots from the internal data structures.
   * <p>
   * Note that clearing the persistence context does not affect the underlying database or any
   * connected data sources. It only removes the entities from the current in-memory state.
   * <p>
   * This method should be called when the persistence context needs to be reset or when a clean
   * state is required.
   * <p>
   * It is recommended to use this method with caution as it might lead to unexpected behavior if
   * not used appropriately.
   */
  public void clear() {
    log.trace("Clearing persistence context");
    entitiesByKey.clear();
    entitiesSnapshotByKey.clear();
  }

  /**
   * Creates a snapshot of an entity.
   *
   * @param entity the entity to create a snapshot for
   * @return an array of objects representing the snapshot of the entity
   */
  private Object[] createEntitySnapshot(T entity) {
    log.trace("Creating a snapshot for entity {}", entity);
    return Arrays.stream(entity.getClass().getDeclaredFields())
      .map(f -> getFieldValues(f, entity))
      .toArray();
  }

  /**
   * Retrieves the value of a field from the given entity object.
   *
   * @param field  the field to retrieve the value from
   * @param entity the object containing the field
   * @return the value of the field, or null if unable to access the field value
   */
  private Object getFieldValues(Field field, Object entity) {
    try {
      field.setAccessible(true);
      return field.get(entity);
    } catch (IllegalAccessException e) {
      log.trace("Unable to access field value.", e);
      return null;
    }
  }
}