package com.bibernate.hoverla.configuration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

import com.bibernate.hoverla.configuration.config.CommonConfig;
import com.bibernate.hoverla.exceptions.ConfigurationException;
import com.bibernate.hoverla.session.SessionFactory;
import com.bibernate.hoverla.session.impl.SessionImpl;

import lombok.Builder;
import lombok.Getter;
/**
 * Configuration is the main configuration class for the Bibernate framework. It provides
 * methods for configuring the framework with different configuration sources, and building a
 * SessionFactory.
 */

@Getter
@Builder
public class Configuration {
  /**
   * The URL variable represents the JDBC URL for establishing a database connection.
   */
  public static final String URL = "bibernate.connection.url";
  /**
   * The USERNAME variable represents the key for retrieving the username property
   * from the properties' configuration. It is used in the buildSessionFactory() method
   * of the Configuration class to establish a database connection.
   */
  public static final String USERNAME = "bibernate.connection.username";
  /**
   * The PASSWORD variable represents the key for retrieving the password property
   * from the properties' configuration.
   */
  public static final String PASSWORD = "bibernate.connection.password";

  /**
   * The packageName variable represents the package name to be used for entity scanning in the Hibernate configuration.
   * It is used in the buildSessionFactory() method of the Configuration class.
   */
  private String packageName;
  /**
   * The properties variable represents a configuration that provides access
   * to JDBC URL and generic properties commonly required in database operations.
   * It is an instance of the {@link CommonConfig} interface.
   */
  private CommonConfig properties;
  /**
   * The `annotatedClasses` variable is a list of classes that have been annotated
   * to be included in the Hibernate session factory configuration.
   * These classes represent the entities that will be mapped to database tables.
   * The annotatedClasses variable is used in the buildSessionFactory() method
   * of the Configuration class to scan and configure the metamodel.
   */
  private List<Class<?>> annotatedClasses;
  /**
   *
   */
  private volatile SessionFactory sessionFactory;

  /**
   * Retrieves the SessionFactory instance. If the SessionFactory
   * is not initialized, it creates a new instance by calling the
   * buildSessionFactory method. The method uses double-checked
   * locking to ensure thread safety.
   *
   * @return The SessionFactory instance.
   */
  public SessionFactory getSessionFactory() {
    if (sessionFactory == null) {
      synchronized (this) {
        if (sessionFactory == null) {
          sessionFactory = buildSessionFactory();
        }
      }
    }
    return sessionFactory;
  }

  /**
   * Builds a SessionFactory instance.
   *
   * @return A new SessionFactory instance.
   * @throws ConfigurationException if failed to create the session factory.
   */
  private SessionFactory buildSessionFactory() {
    try {
      Connection connection = DriverManager.getConnection(
        properties.getProperty(URL),
        properties.getProperty(USERNAME),
        properties.getProperty(PASSWORD)
      );

      //TODO var metamodel = new MetamodelScanner();
      //metamodel.scanPackage(packageName);

      return (SessionFactory) new SessionImpl(connection); // should be replaced in future commits
    } catch (Exception e) {
      throw new ConfigurationException("Failed to create session factory: ", e);
    }
  }
}
