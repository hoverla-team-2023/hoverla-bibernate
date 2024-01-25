package com.bibernate.hoverla.session.impl;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import com.bibernate.hoverla.metamodel.Metamodel;
import com.bibernate.hoverla.session.Session;
import com.bibernate.hoverla.session.SessionFactory;

import lombok.extern.slf4j.Slf4j;

/**
 * The `SessionFactoryImpl` class is an implementation of the `SessionFactory` interface.
 * It is responsible for creating and managing `Session` instances.
 */
@Slf4j
public class SessionFactoryImpl implements SessionFactory {

  /**
   * A map that holds generators for entity classes.
   */
  private final Map<Class<?>, Object> generatorsMap = new HashMap<>();

  /**
   * The metamodel that describes the entities and their relationships.
   */
  private final Metamodel metamodel;

  /**
   * The data source that provides the database connection.
   */
  private final DataSource dataSource;

  /**
   * Constructs a `SessionFactoryImpl` with the specified data source and metamodel.
   *
   * @param dataSource The data source that provides the database connection.
   * @param metamodel  The metamodel that describes the entities and their relationships.
   */
  public SessionFactoryImpl(DataSource dataSource, Metamodel metamodel) {
    this.dataSource = dataSource;
    this.metamodel = metamodel;
  }

  /**
   * Opens a new `Session` instance.
   *
   * @return A new `Session` instance.
   */
  @Override
  public synchronized Session openSession() {
    log.info("Opening new HoverlaSession");
    return new SessionImpl(dataSource);
  }

  /**
   * @return The map of generators for entity classes.
   */
  @Override
  public Map<Class<?>, Object> getGeneratorsMap() {
    return generatorsMap;
  }

  /**
   * Returns the metamodel that describes the entities and their relationships.
   *
   * @return The metamodel.
   */
  @Override
  public Metamodel getMetamodel() {
    return metamodel;
  }

  /**
   * Returns the data source that provides the database connection.
   *
   * @return The data source.
   */
  @Override
  public DataSource getDataSource() {
    return dataSource;
  }

}