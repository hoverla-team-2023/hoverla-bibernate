package com.bibernate.hoverla.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.bibernate.hoverla.exceptions.BibernateSqlException;
import com.bibernate.hoverla.jdbc.types.PostgreSqlJdbcEnumType;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JdbcExecutorImplTest {

  @RegisterExtension
  static PostgresSqlTestExtension DB = new PostgresSqlTestExtension("jdbc-executor/init-jdbc-executor-test.sql", "jdbc-executor/delete-all.sql");

  @Test
  @Order(10)
  void whenUpdateWithCustomJdbcType_thenChangesSaved() {
    inTransaction(DB.getDataSource(), connection -> {
      JdbcExecutorImpl jdbcExecutor = new JdbcExecutorImpl(connection);
      JdbcParameterBinding<Integer> integerJdbcParameterBinding = new JdbcParameterBinding<>(2, PreparedStatement::setObject);
      JdbcParameterBinding<Role> roleJdbcParameterBinding = new JdbcParameterBinding<>(Role.ADMIN, new PostgreSqlJdbcEnumType<>(Role.class));

      jdbcExecutor
        .executeUpdate("UPDATE users SET role = ? where id = ?",
                       new JdbcParameterBinding[] {
                         roleJdbcParameterBinding,
                         integerJdbcParameterBinding }
        );
    });

    String result = inTransaction(DB.getDataSource(), connection -> {
      JdbcExecutorImpl jdbcExecutor = new JdbcExecutorImpl(connection);
      List<Object[]> objects = jdbcExecutor
        .executeSelectQuery("SELECT * FROM users",
                            new JdbcParameterBinding[] {},
                            new JdbcResultExtractor[] { ResultSet::getObject,
                                                        ResultSet::getObject,
                                                        ResultSet::getObject,
                                                        new PostgreSqlJdbcEnumType<>(Role.class) }

        );
      return objects.stream().map(Arrays::toString).collect(Collectors.joining(","));
    });

    Assertions.assertEquals("[1, FirsName1, LastName1, null],[2, FirsName2, LastName2, ADMIN]", result);
  }

  @Test
  @Order(20)
  void whenInsert_thenReturnGeneratedKeys() {
    Object generateKey = inTransaction(DB.getDataSource(), connection -> {
      return new JdbcExecutorImpl(connection)
        .executeUpdateAndReturnGeneratedKeys("INSERT INTO users (first_name, last_name, role) VALUES (?,?,?)",
                                             new JdbcParameterBinding[] {
                                               new JdbcParameterBinding<>("testName", PreparedStatement::setObject),
                                               new JdbcParameterBinding<>("testLastName", PreparedStatement::setObject),
                                               new JdbcParameterBinding<>(Role.ADMIN, new PostgreSqlJdbcEnumType<>(Role.class)) }
        );
    });

    Assertions.assertNotNull(generateKey);
  }

  @Test
  @Order(30)
  void whenInsertSqlWithWrongTable_thenBibernateExceptionIsThrown() {

    inTransaction(DB.getDataSource(), connection -> {
      Assertions.assertThrows(BibernateSqlException.class, () ->

        new JdbcExecutorImpl(connection)
          .executeUpdateAndReturnGeneratedKeys("INSERT INTO persons (first_name, last_name, role) VALUES (?,?,?)",
                                               new JdbcParameterBinding[] {
                                                 new JdbcParameterBinding<>("testName", PreparedStatement::setObject),
                                                 new JdbcParameterBinding<>("testLastName", PreparedStatement::setObject),
                                                 new JdbcParameterBinding<>(Role.ADMIN, new PostgreSqlJdbcEnumType<>(Role.class)) }
          ));
    });

  }

  @Test
  @Order(40)
  void whenUpdateWithWrongTable_thenChangesSaved() {

    inTransaction(DB.getDataSource(), connection -> {
      Assertions.assertThrows(BibernateSqlException.class, () -> {
                                JdbcExecutorImpl jdbcExecutor = new JdbcExecutorImpl(connection);
                                JdbcParameterBinding<Integer> integerJdbcParameterBinding = new JdbcParameterBinding<>(2, PreparedStatement::setObject);
                                JdbcParameterBinding<Role> roleJdbcParameterBinding = new JdbcParameterBinding<>(Role.ADMIN, new PostgreSqlJdbcEnumType<>(Role.class));

                                jdbcExecutor
                                  .executeUpdate("UPDATE users SET rolle = ? where id = ?",
                                                 new JdbcParameterBinding[] {
                                                   roleJdbcParameterBinding,
                                                   integerJdbcParameterBinding }
                                  );
                              }
      );
    });
  }

  private void inTransaction(DataSource dataSource, Consumer<Connection> consumer) {
    try (Connection connection = dataSource.getConnection()) {
      connection.setAutoCommit(false);
      consumer.accept(connection);
      connection.commit();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  private <T> T inTransaction(DataSource dataSource, Function<Connection, T> consumer) {
    try (Connection connection = dataSource.getConnection()) {
      connection.setAutoCommit(false);
      T result = consumer.apply(connection);
      connection.commit();
      return result;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  enum Role {
    ADMIN,
    USER
  }

}







