package com.bibernate.hoverla.session;

import com.bibernate.hoverla.metamodel.EntityMapping;
import com.bibernate.hoverla.session.cache.EntityKey;

public record EntityDetails<T> (EntityMapping entityMapping,
                            EntityKey<T> entityKey,
                            boolean isProxy) {}
