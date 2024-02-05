package com.bibernate.hoverla.query;

import java.util.List;

import com.bibernate.hoverla.session.Session;

/**
 * The Query interface represents a query for retrieving data from a database.
 * See: {@link Session#createQuery(String, Class)}
 *
 * @param <T> The type of the result of the query.
 */
public interface Query<T> {

  /**
   * Sets a parameter for the query.
   *
   * @param parameter The name of the parameter.
   * @param object    The value of the parameter.
   *
   * @return A reference to the same Query instance for method chaining.
   */
  Query<T> setParameter(String parameter, Object object);

  /**
   * Executes the query and returns the result as a list.
   *
   * @return A list of results based on the query.
   */
  List<T> getResult();

}
