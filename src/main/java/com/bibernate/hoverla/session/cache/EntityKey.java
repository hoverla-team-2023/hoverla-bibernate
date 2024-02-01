package com.bibernate.hoverla.session.cache;

import java.util.Optional;

import static com.bibernate.hoverla.utils.EntityUtils.getId;

/**
 * The `EntityKey` record is used to uniquely identify an entity instance.
 * It consists of the entity's class and an identifier (ID).
 */
public record EntityKey<T>(Class<T> entityType, Object id) {

  /**
   * Creates a new `EntityKey` with the specified entity type and ID.
   *
   * @param entityType The class of the entity.
   * @param id         The ID of the entity.
   * @param <T>        The type of the entity.
   * @return A new `EntityKey` instance.
   */
  public static <T> EntityKey<?> of(Class<T> entityType, Object id) {
    return new EntityKey<>(entityType, id);
  }

  public static <T> Optional<EntityKey<T>> valueOf(T entity) {
    return Optional.ofNullable(entity)
      .flatMap(e -> {
        Optional<Object> id = getId(e);
        return id.map(value -> Optional.of(entity.getClass())
          .map(c -> (EntityKey<T>) new EntityKey<>(c, value))
          .orElseThrow());
      });
  }
}