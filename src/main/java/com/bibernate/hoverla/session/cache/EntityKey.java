package com.bibernate.hoverla.session.cache;
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

  /**
   * Creates a new `EntityKey` for the given entity. The entity's class and ID are extracted.
   *
   * @param entity The entity for which to create the key.
   * @param <T>    The type of the entity.
   * @return A new `EntityKey` instance for the entity.
   */
  //  public static <T> EntityKey<T> valueOf(T entity) {
  //    var id = getId(entity);
  //    var entityType = entity.getClass();
  //    return new EntityKey(entityType, id);
  //  }
}