package com.bibernate.hoverla.configuration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;

import com.bibernate.hoverla.configuration.config.CommonConfig;

public class ConfigurationIntegrationTest {

  @Test
  public void testSessionFactoryCreation() {
    try (PostgreSQLContainer<?> postgresqlContainer = new PostgreSQLContainer<>("postgres:latest")
      .withDatabaseName("testdb")
      .withUsername("test")
      .withPassword("test")) {

      postgresqlContainer.start();

      // Load properties and then override the connection details with those from the container
      CommonConfig commonConfig = CommonConfig.of("config.properties");
      commonConfig.setProperty(Configuration.URL, postgresqlContainer.getJdbcUrl());
      commonConfig.setProperty(Configuration.USERNAME, postgresqlContainer.getUsername());
      commonConfig.setProperty(Configuration.PASSWORD, postgresqlContainer.getPassword());

      Configuration configuration = Configuration.builder()
        .properties(commonConfig)
        .build();

      Assertions.assertNotNull(configuration.getSessionFactory());
    }
  }
}
