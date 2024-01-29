package com.bibernate.hoverla.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.postgresql.ds.PGSimpleDataSource;
import org.testcontainers.containers.PostgreSQLContainer;

import com.bibernate.hoverla.jdbc.testability.SqlScriptBatchExecutor;

import lombok.Getter;
import lombok.SneakyThrows;

public class PostgresSqlTestExtension implements Extension, BeforeAllCallback, AfterAllCallback {

  private final Connection connection;
  private final String initScript;
  private final String deleteScript;

  public static final PostgreSQLContainer<? extends PostgreSQLContainer<?>> POSTGRES_SQL_CONTAINER;

  static {
    POSTGRES_SQL_CONTAINER = new PostgreSQLContainer<>("postgres:15-alpine")
      .withDatabaseName("integration-tests-db")
      .withUsername("admin")
      .withPassword("admin");
    POSTGRES_SQL_CONTAINER.start();
  }

  @SneakyThrows
  public PostgresSqlTestExtension(String initScript, String deleteScript) {
    PGSimpleDataSource dataSource = new PGSimpleDataSource();
    dataSource.setURL(POSTGRES_SQL_CONTAINER.getJdbcUrl());
    dataSource.setPassword(POSTGRES_SQL_CONTAINER.getPassword());
    dataSource.setUser(POSTGRES_SQL_CONTAINER.getUsername());

    this.connection = dataSource.getConnection();
    this.initScript = initScript;
    this.deleteScript = deleteScript;

  }

  @Override
  public void beforeAll(ExtensionContext extensionContext) throws Exception {
    SqlScriptBatchExecutor.executeBatchedSQL(initScript, connection, 50);
  }

  @Override
  @SneakyThrows
  public void afterAll(ExtensionContext context) throws SQLException {
    clean();
    if (connection != null) {
      connection.close();
    }
  }

  private void clean() throws Exception {
    SqlScriptBatchExecutor.executeBatchedSQL(deleteScript, connection, 25);
  }

}
