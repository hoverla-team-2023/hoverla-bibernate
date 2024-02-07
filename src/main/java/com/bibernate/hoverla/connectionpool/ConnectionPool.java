package com.bibernate.hoverla.connectionpool;

import java.util.Properties;

import javax.sql.DataSource;

import com.bibernate.hoverla.configuration.Configuration;
import com.zaxxer.hikari.HikariDataSource;

public class ConnectionPool {

  private static final String HIKARI = "hikari";
  public static final String CONNECTION_POOL_TYPE = "bibernate.connection-pool.type";

  /**
   * Retrieves a data source based on the configuration provided.
   *
   * @param config The configuration object containing connection details.
   * @return A DataSource instance configured according to the specified connection type.
   * @throws IllegalArgumentException if an invalid connection pool type is specified.
   */
  public static DataSource getDataSource(Configuration config) {
    var props = config.getProperties();
    switch (props.getProperty(CONNECTION_POOL_TYPE)) {
      case HIKARI:
        var properties = new Properties();
        var hikariDataSource = new HikariDataSource();
        properties.putAll(props.getAllProperties("dataSource"));
        hikariDataSource.setDataSourceProperties( properties);
        return hikariDataSource;
      default:
        throw new IllegalArgumentException("Invalid connection pool type");
    }
  }
}
