package com.bibernate.hoverla.session.cache;

public record EntityKey(Class<?> entityType, Object id) {
}
