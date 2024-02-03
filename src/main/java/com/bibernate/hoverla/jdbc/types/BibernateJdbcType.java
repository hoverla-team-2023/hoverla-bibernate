package com.bibernate.hoverla.jdbc.types;

import com.bibernate.hoverla.jdbc.JdbcParameterBinder;
import com.bibernate.hoverla.jdbc.JdbcResultExtractor;

/**
 * The JdbcType interface extends both {@link JdbcResultExtractor} and {@link JdbcParameterBinder}
 * interfaces, defining a contract for working with a specific data type T in the context of JDBC.
 * It allows for extracting and binding values of type T to/from a ResultSet and PreparedStatement
 * for SQL queries and updates.
 *
 * @param <T> The data type to work with in the JDBC context.
 */
public interface BibernateJdbcType<T> extends JdbcResultExtractor<T>, JdbcParameterBinder<T> {
}