package com.bibernate.hoverla.configuration;

import java.sql.Connection;

import javax.sql.DataSource;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;

import com.bibernate.hoverla.configuration.config.CommonConfig;
import com.bibernate.hoverla.session.SessionFactory;
import com.bibernate.hoverla.session.SessionFactoryImplementor;

public class ConfigurationIntegrationTest {

  private static final String URL = "bibernate.dataSource.jdbcUrl";
  private static final String USERNAME = "bibernate.dataSource.username";
  private static final String PASSWORD = "bibernate.dataSource.password";

  @Test
  public void testSessionFactoryCreation() {
    try (PostgreSQLContainer<?> postgresqlContainer = new PostgreSQLContainer<>("postgres:latest")
      .withDatabaseName("testdb")
      .withUsername("test")
      .withPassword("test")) {

      postgresqlContainer.start();

      // Load properties and then override the connection details with those from the container
      CommonConfig commonConfig = CommonConfig.of("config.yml");
      commonConfig.setProperty(URL, postgresqlContainer.getJdbcUrl());
      commonConfig.setProperty(USERNAME, postgresqlContainer.getUsername());
      commonConfig.setProperty(PASSWORD, postgresqlContainer.getPassword());

      Configuration configuration = Configuration.builder()
        .packageName(this.getClass().getPackageName())
        .properties(commonConfig)
        .build();

      final SessionFactoryImplementor sessionFactory = (SessionFactoryImplementor) configuration.getSessionFactory();
      Assertions.assertNotNull(sessionFactory.getDataSource());
    }
  }
}
