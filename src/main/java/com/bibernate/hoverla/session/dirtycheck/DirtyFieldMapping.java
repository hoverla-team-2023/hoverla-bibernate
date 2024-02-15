package com.bibernate.hoverla.session.dirtycheck;

import com.bibernate.hoverla.metamodel.FieldMapping;
/**
 * A record that represents a dirty field mapping. This record is used to hold a field mapping and its corresponding value.
 *
 * @param <T> The type of the field value.
 */
public record DirtyFieldMapping<T>(FieldMapping<T> fieldMapping, Object value) {
}
