package com.bibernate.hoverla.metamodel;

import java.util.Map;

import lombok.Data;

/**
 * The Metamodel serves as a central repository for storing metadata about Java entity classes and their mappings to database tables and columns
 * <p/>
 * Metamodel that represents scanned metadata of entities. Each entity metadata is represented by a {@link EntityMapping}.
 * Each {@link EntityMapping} contains information about its related table name and columns
 *
 * <p>
 * Key Features:
 * - Eliminates Runtime Reflection: Avoids runtime reflection by abstracting metadata retrieval logic.
 * - Centralized Source for Table and Column Names: Provides a single source for table and column names.
 * - Reduced Coupling: Encapsulates metadata, reducing coupling with specific implementation details.
 * - Simplified Development: Abstracts away complexity, enabling faster feature development.
 * - Early Validation: Facilitates early validation of entity definitions and associations.
 * - Ensures SQL Query Correctness: Maintains a consistent order of columns, ensuring SQL query correctness.
 * </p>
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
