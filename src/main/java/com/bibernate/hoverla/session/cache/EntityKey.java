package com.bibernate.hoverla.session.cache;

public record EntityKey<T>(Class<T> entityType, Object id) {


}
