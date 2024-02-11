package com.bibernate.hoverla.configuration;

import java.util.List;

import com.bibernate.hoverla.configuration.config.CommonConfig;
import com.bibernate.hoverla.connectionpool.ConnectionPool;
import com.bibernate.hoverla.exceptions.ConfigurationException;
import com.bibernate.hoverla.jdbc.types.provider.JdbcTypeProviderImpl;
import com.bibernate.hoverla.metamodel.scan.MetamodelScanner;
import com.bibernate.hoverla.session.SessionFactory;
import com.bibernate.hoverla.session.SessionFactoryImpl;

import lombok.Builder;
import lombok.Getter;

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
          sessionFactory = buildSessionFactory();
        }
      }
    }
    return sessionFactory;
  }

  private SessionFactory buildSessionFactory() {
    try {
      var metamodel = new MetamodelScanner(new JdbcTypeProviderImpl());
      var dataSource = ConnectionPool.getDataSource(this);
      return new SessionFactoryImpl(dataSource, metamodel.scanPackage(packageName));
    } catch (Exception e) {
      throw new ConfigurationException("Failed to create session factory: ", e);
    }
  }
}
