package com.bibernate.hoverla.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.bibernate.hoverla.exceptions.BibernateSqlException;
import com.bibernate.hoverla.session.SessionImplementor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * The JdbcExecutorImpl class is an implementation of the JdbcExecutor interface, which provides methods
 * for executing SQL queries and updates using JDBC. This implementation allows to perform various
 * database operations, including select queries, insertions, updates, deletions, and working with
 * generated keys, using a provided JDBC Connection.
 */
@Slf4j
@RequiredArgsConstructor
public class JdbcExecutorImpl implements JdbcExecutor {

  private final SessionImplementor sessionImplementor;

  @Override
  public List<Object[]> executeSelectQuery(String sqlTemplate, JdbcParameterBinding<?>[] bindValues, JdbcResultExtractor<?>[] resultExtractors) {
    List<Object[]> results = new ArrayList<>();
    log.debug("Executing query: {}", sqlTemplate);

    try (PreparedStatement preparedStatement = sessionImplementor.getConnection().prepareStatement(sqlTemplate)) {
      bindParameters(preparedStatement, bindValues);

      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        while (resultSet.next()) {
          Object[] resultRow = extractResultRow(resultSet, resultExtractors);
          results.add(resultRow);
        }
      }

      return results;
    } catch (SQLException sqlException) {
      throw new BibernateSqlException(sqlException.getMessage(), sqlException);
    }
  }

  @Override
  public Object executeUpdateAndReturnGeneratedKeys(String sqlTemplate, JdbcParameterBinding<?>[] bindValues, JdbcResultExtractor<?> jdbcResultExtractor) {
    log.debug("Executing update query and returning generated keys: {}", sqlTemplate);

    try (PreparedStatement preparedStatement = sessionImplementor.getConnection().prepareStatement(sqlTemplate, PreparedStatement.RETURN_GENERATED_KEYS)) {
      bindParameters(preparedStatement, bindValues);
      preparedStatement.executeUpdate();
      ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
      Object generatedKey = null;
      if (generatedKeys.next()) {
        generatedKey = jdbcResultExtractor.extractData(generatedKeys, 1);
      }
      if (generatedKey == null) {
        throw new BibernateSqlException("Can not obtain generated keys");
      }
      return generatedKey;
    } catch (SQLException sqlException) {
      throw new BibernateSqlException(sqlException.getMessage(), sqlException);
    }
  }

  @Override
  public int executeUpdate(String sqlTemplate, JdbcParameterBinding<?>[] bindValues) {
    log.debug("Executing update query: {}", sqlTemplate);
    try (PreparedStatement preparedStatement = sessionImplementor.getConnection().prepareStatement(sqlTemplate)) {
      bindParameters(preparedStatement, bindValues);
      return preparedStatement.executeUpdate();
    } catch (SQLException sqlException) {
      throw new BibernateSqlException(sqlException.getMessage(), sqlException);
    }
  }

  private <T> void bindParameters(PreparedStatement preparedStatement, JdbcParameterBinding<?>[] bindValues) throws SQLException {
    if (bindValues != null && bindValues.length > 0) {
      for (int i = 0; i < bindValues.length; i++) {
        bindValues[i].doBind(preparedStatement, i + 1);
      }
    }
  }

  private Object[] extractResultRow(ResultSet resultSet, JdbcResultExtractor<?>[] resultExtractors) throws SQLException {
    Object[] resultRow = new Object[resultSet.getMetaData().getColumnCount()];
    for (int i = 0; i < resultRow.length; i++) {
      resultRow[i] = resultExtractors[i].extractData(resultSet, i + 1);
    }
    return resultRow;
  }

}
