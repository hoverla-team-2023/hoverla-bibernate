package com.bibernate.hoverla.configuration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;

import com.bibernate.hoverla.configuration.config.CommonConfig;
import com.bibernate.hoverla.exceptions.ConfigurationException;
import com.bibernate.hoverla.exceptions.InvalidEntityDeclarationException;
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
        .packageName("com.bibernate.hoverla.configuration.domain")
        .properties(commonConfig)
        .build();

      final SessionFactoryImplementor sessionFactory = (SessionFactoryImplementor) configuration.getSessionFactory();
      Assertions.assertNotNull(sessionFactory.getDataSource());
    }
  }

  @Test
  public void testSessionFactoryCreationInvalidManyToOne() {
    String packageName = "com.bibernate.hoverla.configuration.domain_many_to_one_issue";
    assertThrowConfigurationException(packageName);
  }

  @Test
  public void testSessionFactoryCreationInvalidOneToManyValidationMappedByFailed() {
    String packageName = "com.bibernate.hoverla.configuration.domain_one_to_many_mappedby_issue";
    assertThrowConfigurationException(packageName);
  }

  @Test
  public void testSessionFactoryCreationInvalidOneToManyValidationTypeFailed() {
    String packageName = "com.bibernate.hoverla.configuration.domain_one_to_many_issue";
    assertThrowConfigurationException(packageName);
  }

  private void assertThrowConfigurationException(String packageName) {
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
        .packageName(packageName)
        .properties(commonConfig)
        .build();

      ConfigurationException configurationException = Assertions.assertThrows(ConfigurationException.class, configuration::getSessionFactory);
      Assertions.assertEquals(InvalidEntityDeclarationException.class, configurationException.getCause().getClass());
    }
  }

}
