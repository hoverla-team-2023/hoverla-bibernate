package com.bibernate.hoverla.session.dirtycheck;

import com.bibernate.hoverla.metamodel.FieldMapping;

public record DirtyFieldMapping<T>(FieldMapping<T> fieldMapping, T value) {
}
