package com.bibernate.hoverla.session;

import com.bibernate.hoverla.metamodel.EntityMapping;
import com.bibernate.hoverla.session.cache.EntityKey;

public record EntityDetails(EntityMapping entityMapping,
                            EntityKey entityKey,
                            boolean isProxy) {}
