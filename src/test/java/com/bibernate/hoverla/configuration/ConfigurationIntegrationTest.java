package com.bibernate.hoverla.configuration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;

import com.bibernate.hoverla.configuration.config.CommonConfig;

public class ConfigurationIntegrationTest {
  private static final String URL = "dataSource.jdbcUrl";
  private static final String USERNAME = "dataSource.username";
  private static final String PASSWORD = "dataSource.password";

  @Test
  public void testSessionFactoryCreation() {
    try (PostgreSQLContainer<?> postgresqlContainer = new PostgreSQLContainer<>("postgres:latest")
      .withDatabaseName("testdb")
      .withUsername("test")
      .withPassword("test")) {

      postgresqlContainer.start();

      // Load properties and then override the connection details with those from the container
      CommonConfig commonConfig = CommonConfig.of("config.properties");
      commonConfig.setProperty(URL, postgresqlContainer.getJdbcUrl());
      commonConfig.setProperty(USERNAME, postgresqlContainer.getUsername());
      commonConfig.setProperty(PASSWORD, postgresqlContainer.getPassword());

      Configuration configuration = Configuration.builder()
        .properties(commonConfig)
        .build();

      Assertions.assertNotNull(configuration.getSessionFactory());
      Assertions.assertNotNull(configuration.getSessionFactory().getDataSource());
    }
  }
}
