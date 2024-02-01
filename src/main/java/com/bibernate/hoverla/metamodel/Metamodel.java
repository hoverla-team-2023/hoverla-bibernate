package com.bibernate.hoverla.metamodel;

import java.util.Map;

import lombok.Data;

/**
 * Metamodel that represents scanned entities with their metadata. Each entity is represented by a {@link EntityMapping}.
 * Each {@link EntityMapping} contains information about its related table name and columns
 *
 * @see com.bibernate.hoverla.annotations.Entity
 * @see com.bibernate.hoverla.metamodel.scan.MetamodelScanner
 * @see EntityMapping
 * @see FieldMapping
 */
@Data
public class Metamodel {

  private final Map<Class<?>, EntityMapping> entityMappingMap;

  /**
   * Merge current {@link Metamodel#entityMappingMap} with the provided {@link Metamodel#entityMappingMap}
   *
   * @return current instance of {@link Metamodel} with merged inner info
   */
  public Metamodel merge(Metamodel metamodel) {
    entityMappingMap.putAll(metamodel.entityMappingMap);
    return this;
  }

}
