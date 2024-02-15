package com.bibernate.hoverla.session.cache;
/**
 * A record representing a unique key for an entity.
 * This key consists of the entity type and the identifier of the entity.
 *
 * @param <T> The type of the entity.
 */
public record EntityKey<T>(Class<T> entityType, Object id) {


}
