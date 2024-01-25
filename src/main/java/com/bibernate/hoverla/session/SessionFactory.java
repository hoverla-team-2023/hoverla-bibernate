package com.bibernate.hoverla.session;

import java.util.Map;

import javax.sql.DataSource;

import com.bibernate.hoverla.metamodel.Metamodel;

/**
 * The `SessionFactory` interface is responsible for creating `Session` instances.
 * It provides access to the `DataSource` and `Metamodel`, which are used to
 * configure and manage the sessions.
 */
public interface SessionFactory {

  /**
   * Opens a new `Session` instance.
   *
   * @return A new `Session` instance.
   */
  Session openSession();

  /**
   * Returns a map of generators for entity classes.
   *
   * @return A map where the keys are entity classes and the values are generators.
   */
  Map<Class<?>, Object> getGeneratorsMap();

  /**
   * Returns the `Metamodel` that describes the entities and their relationships.
   *
   * @return The `Metamodel` instance.
   */
  Metamodel getMetamodel();

  /**
   * Returns the `DataSource` that provides the database connection.
   *
   * @return The `DataSource` instance.
   */
  DataSource getDataSource();

}