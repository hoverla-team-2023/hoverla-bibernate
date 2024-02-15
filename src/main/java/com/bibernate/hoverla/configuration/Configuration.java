package com.bibernate.hoverla.configuration;

import java.util.List;

import com.bibernate.hoverla.configuration.config.CommonConfig;
import com.bibernate.hoverla.connectionpool.ConnectionPool;
import com.bibernate.hoverla.exceptions.ConfigurationException;
import com.bibernate.hoverla.jdbc.types.provider.JdbcTypeProviderImpl;
import com.bibernate.hoverla.metamodel.MetamodelPostValidatorImpl;
import com.bibernate.hoverla.metamodel.MetamodelValidator;
import com.bibernate.hoverla.metamodel.scan.MetamodelScanner;
import com.bibernate.hoverla.session.SessionFactory;
import com.bibernate.hoverla.session.SessionFactoryImpl;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Configuration class for managing application settings and initializing resources
 * such as the session factory for database operations.
 */
@Slf4j
@Getter
@Builder
public class Configuration {

  private String packageName;
  private CommonConfig properties;
  private List<Class<?>> annotatedClasses;
  private volatile SessionFactory sessionFactory;

  /**
   * Retrieves the session factory, initializing it if necessary.
   *
   * @return The session factory instance.
   */
  public SessionFactory getSessionFactory() {
    if (sessionFactory == null) {
      synchronized (this) {
        if (sessionFactory == null) {
          log.info("Initializing session factory");
          sessionFactory = buildSessionFactory();
          log.info("Session factory initialized");
        }
      }
    }
    return sessionFactory;
  }

  private SessionFactory buildSessionFactory() {
    try {
      var metamodelScanner = new MetamodelScanner(new JdbcTypeProviderImpl());
      List<MetamodelValidator> validators = List.of(new MetamodelPostValidatorImpl());

      log.debug("Start scanning packageName: " + packageName);
      var metamodel = metamodelScanner.scanPackage(packageName)
        .merge(metamodelScanner.scanEntities(annotatedClasses));
      validators.forEach(validator -> validator.validate(metamodel));
      log.debug("Metamodel scanner created successfully: " + metamodel);

      var dataSource = ConnectionPool.getDataSource(this);
      log.debug("DataSource created successfully: " + dataSource);

      return new SessionFactoryImpl(dataSource, metamodel);
    } catch (Exception e) {
      log.error("Failed to create session factory: " + e.getMessage());
      throw new ConfigurationException("Failed to create session factory: ", e);
    }
  }

}
