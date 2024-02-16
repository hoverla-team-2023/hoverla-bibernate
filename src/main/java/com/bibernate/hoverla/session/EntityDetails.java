package com.bibernate.hoverla.session;

import com.bibernate.hoverla.metamodel.EntityMapping;
import com.bibernate.hoverla.session.cache.EntityKey;

/**
 * Represents details about an entity, including its mapping, key, and whether it's a proxy.
 *
 * @param <T> the type of the entity
 */
public record EntityDetails<T>(EntityMapping entityMapping,
                               EntityKey<T> entityKey,
                               boolean isProxy) {}
