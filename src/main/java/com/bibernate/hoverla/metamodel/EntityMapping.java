package com.bibernate.hoverla.metamodel;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.bibernate.hoverla.exceptions.InvalidEntityDeclarationException;
import com.bibernate.hoverla.jdbc.types.BibernateJdbcType;

import lombok.Getter;

/**
 * Represents a single entity in the metamodel. It has information about the entity's related table name, column names, primary key, etc.
 *
 * <p>
 * This class provides a mapping between entity fields and database columns, allowing access to metadata associated with the entity.
 * </p>
 *
 * @see FieldMapping
 * @see Metamodel
 */
@Getter
public class EntityMapping {

  /**
   * The class representing the entity.
   */
  private final Class<?> entityClass;

  /**
   * The name of the table associated with the entity.
   */
  private final String tableName;

  /**
   * A mapping between field names and their corresponding {@link FieldMapping}.
   * Utilizes a {@link LinkedHashMap} to preserve the order of mappings.
   */
  private final Map<String, FieldMapping<?>> fieldNameMappingMap = new LinkedHashMap<>();

  public EntityMapping(Class<?> entityClass) {
    this(entityClass, entityClass.getSimpleName());
  }

  public EntityMapping(Class<?> entityClass, String tableName) {
    this.entityClass = entityClass;
    this.tableName = tableName;
  }

  /**
   * Retrieves the primary key mapping from the fieldMappingMap.
   *
   * @return The primary key mapping.
   *
   * @throws InvalidEntityDeclarationException If no primary key is declared in the fieldMappingMap.
   */

  public FieldMapping<?> getPrimaryKeyMapping() {
    return fieldNameMappingMap.values().stream()
      .filter(FieldMapping::isPrimaryKey)
      .findAny()
      .orElseThrow(() -> new InvalidEntityDeclarationException("No primary key id declared"));//should never happen
  }

  public void addFieldMapping(String fieldName, FieldMapping<?> fieldMapping) {
    fieldNameMappingMap.put(fieldName, fieldMapping);
  }

  public FieldMapping<?> getFieldMapping(String fieldName) {
    return fieldNameMappingMap.get(fieldName);
  }

  public List<FieldMapping<?>> getFieldMappings(Predicate<? super FieldMapping<?>> predicate) {
    return getFieldNameMappingMap()
      .values()
      .stream()
      .filter(predicate)
      .toList();
  }

  public Optional<FieldMapping<?>> getFieldMappingWithOptimisticLock() {
    return fieldNameMappingMap.values().stream()
      .filter(FieldMapping::isOptimisticLock)
      .findFirst();
  }

  public String getColumnNames() {
    return getFieldMappings(mapping -> !mapping.isOneToMany())
      .stream()
      .map(FieldMapping::getColumnName)
      .collect(Collectors.joining(", "));
  }

  public List<? extends BibernateJdbcType<?>> getJdbcTypes() {
    return getFieldMappings(mapping -> !mapping.isOneToMany())
      .stream()
      .map(FieldMapping::getJdbcType)
      .collect(Collectors.toList());
  }

}
