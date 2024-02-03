package com.bibernate.hoverla.jdbc.types.provider;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.jupiter.api.Test;

import com.bibernate.hoverla.exceptions.BibernateException;
import com.bibernate.hoverla.jdbc.types.BibernateJdbcType;
import com.bibernate.hoverla.jdbc.types.PostgreSqlJdbcEnumType;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JdbcTypeProviderImplTest {

  @Test
  void shouldReturnNewInstanceOfJdbcType() {
    JdbcTypeProvider provider = new JdbcTypeProviderImpl();

    Class<BibernateJdbcType<String>> jdbcTypeClass = null;
    Class<String> fieldType = String.class;

    BibernateJdbcType<String> jdbcType = provider.getInstance(jdbcTypeClass, fieldType);

    assertNotNull(jdbcType);
  }

  @Test
  <T extends Enum<T>> void testGetInstanceReturnsJdbcTypeForPostgreSqlJdbcEnumTypeAndTestEnum() {
    JdbcTypeProvider provider = new JdbcTypeProviderImpl();

    Class<? extends BibernateJdbcType<T>> jdbcTypeClass = (Class<? extends BibernateJdbcType<T>>) (Class<?>) PostgreSqlJdbcEnumType.class;
    Class<TestEnum> fieldType = TestEnum.class;
    BibernateJdbcType<?> jdbcType = provider.getInstance(jdbcTypeClass,
                                                         fieldType);

    assertNotNull(jdbcType);
  }

  @Test
  void shouldThrowBibernateExceptionWhenInstanceCreationFails() {
    JdbcTypeProvider provider = new JdbcTypeProviderImpl();

    Class<JdbcTypeWithNoSuitableConstructor> jdbcTypeClass = JdbcTypeWithNoSuitableConstructor.class;
    Class<String> fieldType = String.class;

    assertThrows(BibernateException.class, () -> provider.getInstance(jdbcTypeClass, fieldType));
  }

  static enum TestEnum {
    ENUM_VALUE_1
  }

  static class JdbcTypeWithNoSuitableConstructor implements BibernateJdbcType<String> {

    private JdbcTypeWithNoSuitableConstructor() {

    }

    @Override
    public void bindParameter(PreparedStatement preparedStatement, int index, String value) throws SQLException {

    }

    @Override
    public String extractData(ResultSet resultSet, int index) throws SQLException {
      return null;
    }

  }

}