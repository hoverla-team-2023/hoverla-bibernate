package com.bibernate.hoverla.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.bibernate.hoverla.jdbc.types.PostgreSqlJdbcEnumType;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JdbcExecutorImplTest {

  @RegisterExtension
  static PostgresSqlTestExtension DB = new PostgresSqlTestExtension("init-jdbc-executor-test.sql", "delete-all.sql");

  JdbcExecutorImpl jdbcExecutor = new JdbcExecutorImpl(DB.getConnection());

  @Test
  @Order(10)
  void whenUpdateWithCustomJdbcType_thenChangesSaved() throws SQLException {
    JdbcParameterBinding<Integer> integerJdbcParameterBinding = new JdbcParameterBinding<>(2, PreparedStatement::setObject);
    JdbcParameterBinding<Role> roleJdbcParameterBinding = new JdbcParameterBinding<>(Role.ADMIN, new PostgreSqlJdbcEnumType<>(Role.class));
    jdbcExecutor
      .executeUpdate("UPDATE users SET role = ? where id = ?",
                     new JdbcParameterBinding[] {
                       roleJdbcParameterBinding,
                       integerJdbcParameterBinding }
      );
    DB.getConnection().commit();

    List<Object[]> objects = jdbcExecutor
      .executeSelectQuery("SELECT * FROM users",
                          new JdbcParameterBinding[] {},
                          new JdbcResultExtractor[] { ResultSet::getObject,
                                                      ResultSet::getObject,
                                                      ResultSet::getObject,
                                                      new PostgreSqlJdbcEnumType<>(Role.class) }

      );
    String select = objects.stream().map(Arrays::toString)
      .collect(Collectors.joining(","));

    Assertions.assertEquals("[1, FirsName1, LastName1, null],[2, FirsName2, LastName2, ADMIN]", select);
  }

  @Test
  @Order(20)
  void whenInsert_thenReturnGeneratedKeys() throws SQLException {
    DB.getConnection().setAutoCommit(false);

    Object object = jdbcExecutor
      .executeUpdateAndReturnGeneratedKeys("INSERT INTO users (first_name, last_name, role) VALUES (?,?,?)",
                                           new JdbcParameterBinding[] {
                                             new JdbcParameterBinding<>("testName", PreparedStatement::setObject),
                                             new JdbcParameterBinding<>("testLastName", PreparedStatement::setObject),
                                             new JdbcParameterBinding<>(Role.ADMIN, new PostgreSqlJdbcEnumType<>(Role.class)) }
      );

    DB.getConnection().commit();

    Assertions.assertNotNull(object);
  }

  enum Role {
    ADMIN,
    USER
  }

}







