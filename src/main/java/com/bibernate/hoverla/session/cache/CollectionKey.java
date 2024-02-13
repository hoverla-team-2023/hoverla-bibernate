package com.bibernate.hoverla.session.cache;

public record CollectionKey<T>(Class<T> entityType, Object id, String collectionName) {}
