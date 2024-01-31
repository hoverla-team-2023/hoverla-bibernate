package com.bibernate.hoverla.metamodel;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import lombok.Getter;

/**
 * Represents a single entity in the metamodel. It has information about the entity's related table name, column names, primary key, etc.
 *
 * @see FieldMapping
 * @see Metamodel
 */
@Getter
public class EntityMapping {

  private final Class<?> entityClass;
  private final String tableName;

  //  Use LinkedHashMap to preserve column order.
  private final Map<String, FieldMapping<?>> fieldMappingMap = new LinkedHashMap<>();

  public EntityMapping(Class<?> entityClass) {
    this(entityClass, entityClass.getSimpleName());
  }

  public EntityMapping(Class<?> entityClass, String tableName) {
    this.entityClass = entityClass;
    this.tableName = tableName;
  }

  public Optional<FieldMapping<?>> getPrimaryKeyMappings() {
    return fieldMappingMap.values().stream()
      .filter(FieldMapping::isPrimaryKey)
      .findAny();
  }

  public void addFieldMapping(String fieldName, FieldMapping<?> fieldMapping) {
    fieldMappingMap.put(fieldName, fieldMapping);
  }

}
