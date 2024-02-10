package com.bibernate.hoverla.jdbc.types;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * A default implementation of the {@link BibernateJdbcType} interface used when no specific JDBC type is specified.
 */
public class DefaultBibernateJdbcTypeImpl implements BibernateJdbcType<Object> {

  /**
   * Binds a value to a JDBC parameter in a {@link java.sql.PreparedStatement}.
   *
   * @param preparedStatement The prepared statement to bind the value to.
   * @param index             The index of the parameter in the prepared statement.
   * @param object            The value to bind to the parameter.
   *
   * @throws SQLException If an SQL exception occurs while binding the parameter.
   */
  @Override
  public void bindParameter(PreparedStatement preparedStatement, int index, Object object) throws SQLException {
    preparedStatement.setObject(index, object);
  }

  /**
   * Extracts data of type {@code Object} from a {@link ResultSet} at a specified index.
   *
   * @param resultSet The result set from which to extract data.
   * @param index     The index of the column in the result set.
   *
   * @return The extracted data as an {@code Object}.
   *
   * @throws SQLException If an SQL exception occurs while extracting data.
   */
  @Override
  public Object extractData(ResultSet resultSet, int index) throws SQLException {
    return resultSet.getObject(index);
  }

}
