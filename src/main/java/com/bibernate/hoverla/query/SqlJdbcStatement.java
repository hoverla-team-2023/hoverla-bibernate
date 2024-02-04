package com.bibernate.hoverla.query;

import com.bibernate.hoverla.jdbc.JdbcParameterBinding;
import com.bibernate.hoverla.jdbc.JdbcResultExtractor;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents a SQL statement that can be executed against a JDBC database.
 * This class encapsulates the SQL template, the ordered parameters for binding,
 * and the result extractors for mapping the query results.
 */
@Getter
@AllArgsConstructor
public class SqlJdbcStatement {

  /**
   * The SQL template string that represents the SQL statement.
   * This string may contain placeholders for parameters.
   */
  private String sqlTemplate;

  /**
   * An array of {@link JdbcParameterBinding} objects that represent the parameters
   * to bind to the SQL statement. The order of these parameters corresponds to the
   * order of placeholders in the SQL template.
   */
  private JdbcParameterBinding<?>[] getOrderedParameters;

  /**
   * An array of {@link JdbcResultExtractor} objects that are used to extract the
   * results of the SQL statement into objects of type T.
   */
  private JdbcResultExtractor<?>[] jdbcResultExtractors;

}
