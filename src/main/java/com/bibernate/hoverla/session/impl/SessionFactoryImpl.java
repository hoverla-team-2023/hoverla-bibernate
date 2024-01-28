package com.bibernate.hoverla.session.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import com.bibernate.hoverla.exceptions.BibernateException;
import com.bibernate.hoverla.metamodel.Metamodel;
import com.bibernate.hoverla.session.Session;
import com.bibernate.hoverla.session.SessionFactory;
import com.bibernate.hoverla.session.service.HoverlaSessionService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * The `SessionFactoryImpl` class is an implementation of the `SessionFactory` interface.
 * It is responsible for creating and managing `Session` instances.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
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

  private static volatile SessionFactoryImpl sessionFactoryInstance;

  /**
   * Constructs a `SessionFactoryImpl` with the specified data source and metamodel.
   *
   * @param dataSource The data source that provides the database connection.
   *
   */

//  public static SessionFactoryImpl getSessionFactoryInstance(DataSource dataSource) {
//    if (sessionFactoryInstance == null) {
//      synchronized (SessionFactoryImpl.class) {
//        if (sessionFactoryInstance == null) {
//          log.info("Add Hoverla Logo");
//          sessionFactoryInstance = new SessionFactoryImpl(dataSource);
//        }
//      }
//    }
//    return sessionFactoryInstance;
//  }

  /**
   * Opens a new `Session` instance.
   *
   * @return A new `Session` instance.
   */
  @Override
  public  Session openSession() {
    log.info("Opening new HoverlaSession");
    try {
      Connection connection = dataSource.getConnection();
      HoverlaSessionService hoverlaSessionService = new HoverlaSessionService(connection);
      return new SessionImpl(connection, hoverlaSessionService);
    } catch (SQLException e) {
      throw new BibernateException("An error occurred while opening session", e);
    }
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