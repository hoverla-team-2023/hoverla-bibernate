package com.bibernate.hoverla.session;

import com.bibernate.hoverla.metamodel.EntityMapping;
import com.bibernate.hoverla.session.cache.EntityKey;
/**
 * A record that holds the details of an entity.
 * This record is used to encapsulate the entity mapping, entity key, and a flag indicating whether the entity is a proxy.
 *
 * @param <T> The type of the entity.
 * @param entityMapping The entity mapping that describes the structure of the entity.
 * @param entityKey The entity key that uniquely identifies the entity.
 * @param isProxy A boolean flag indicating whether the entity is a proxy or not.
 */
public record EntityDetails<T> (EntityMapping entityMapping,
                            EntityKey<T> entityKey,
                            boolean isProxy) {}
