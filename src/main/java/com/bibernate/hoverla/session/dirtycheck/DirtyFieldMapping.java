package com.bibernate.hoverla.session.dirtycheck;

import com.bibernate.hoverla.metamodel.FieldMapping;

public record DirtyFieldMapping<T>(FieldMapping<T> fieldMapping, Object value) {

  @SuppressWarnings("unchecked")
  public static <T> DirtyFieldMapping<T> of(FieldMapping<?> fieldMapping, Object value) {
    return (DirtyFieldMapping<T>) new DirtyFieldMapping<>(fieldMapping, value);
  }

}
