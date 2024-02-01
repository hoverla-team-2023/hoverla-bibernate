package com.bibernate.hoverla.configuration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

import com.bibernate.hoverla.configuration.config.CommonConfig;
import com.bibernate.hoverla.exceptions.ConfigurationException;
import com.bibernate.hoverla.session.SessionFactory;
import com.bibernate.hoverla.session.SessionFactoryImpl;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Configuration {
  public static final String URL = "bibernate.connection.url";
  public static final String USERNAME = "bibernate.connection.username";
  public static final String PASSWORD = "bibernate.connection.password";

  private String packageName;
  private CommonConfig properties;
  private List<Class<?>> annotatedClasses;
  private volatile SessionFactory sessionFactory;

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
      Connection connection = DriverManager.getConnection(
        properties.getProperty(URL),
        properties.getProperty(USERNAME),
        properties.getProperty(PASSWORD)
      );

      //TODO var metamodel = new MetamodelScanner();
      //metamodel.scanPackage(packageName);

      return new SessionFactoryImpl(connection);
    } catch (Exception e) {
      throw new ConfigurationException("Failed to create session factory: ", e);
    }
  }
}
