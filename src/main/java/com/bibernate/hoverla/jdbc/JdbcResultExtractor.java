package com.bibernate.hoverla.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * The JdbcResultExtractor interface defines a contract for extracting data of type T from a
 * ResultSet at a specified index. It is typically used in conjunction with the {@link JdbcExecutor}
 * interface for processing the results of SQL queries.
 *
 * @param <T> The type of data to be extracted from the ResultSet.
 */
public interface JdbcResultExtractor<T> {

  /**
   * Extracts data of type T from the specified ResultSet at the given index.
   *
   * @param resultSet The ResultSet from which to extract data.
   * @param index     The index of the column in the ResultSet from which to extract data.
   *
   * @return The extracted data of type T.
   *
   * @throws SQLException If an SQL exception occurs while extracting the data.
   */
  T extractData(ResultSet resultSet, int index) throws SQLException;

}
