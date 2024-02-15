package com.bibernate.hoverla.session.cache;
/**
 * A record representing a unique key for a collection within an entity.
 * This key consists of the entity type, the identifier of the entity, and the name of the collection.
 *
 * @param <T> The type of the entity.
 */
public record CollectionKey<T>(Class<T> entityType, Object id, String collectionName) {}
