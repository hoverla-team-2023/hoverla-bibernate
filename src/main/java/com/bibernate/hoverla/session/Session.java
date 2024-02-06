package com.bibernate.hoverla.session;

import com.bibernate.hoverla.exceptions.BibernateBqlException;
import com.bibernate.hoverla.query.Query;

public interface Session extends AutoCloseable {

  <T> T find(Class<T> entityClass, Object id);

  /**
   * Creates a query for executing custom queries using an object-oriented query language.
   * <p/>
   * This method enables the construction and execution of custom queries using a specialized object-oriented query language.
   * The language supports parameterization, allowing the definition and use of parameters in queries for dynamic values.
   * Additionally, it provides support for logical operators (AND, OR), and comparison operators (<, >, <=, >=, =) for
   * filtering and retrieving data from a database or data source. It also offers grouping with parentheses and membership
   * checks (IN).
   * <p/>
   * An example of how to use this method:
   * <p/>
   * <pre>{@code
   * List<MyEntity> result = session.createQuery("WHERE age > :ageParam AND name = :nameParam", MyEntity.class)
   *      .setParameter("ageParam", 30)
   *      .setParameter("nameParam", "John")
   *      .getResult();
   * }</pre>
   * <p/>
   * The "IN" condition is handled as follows:
   * <ul>
   *   <li>If the collection parameter used in the "IN" condition is empty or null, the condition is ignored, and the result
   *      will not be filtered by the "IN" condition.</li>
   *   <li> If the collection contains values, the "IN" condition is applied to match values in the specified column.</li>
   * </ul>
   *
   * <p>An example of using the "IN" condition:</p>
   *
   * <pre>{@code
   * List<MyEntity> result = session.createQuery("WHERE id IN :ids", MyEntity.class)
   *     .setParameter("ids", List.of(1L, 2L, 3L))
   *     .getResult();
   * }</pre>
   *
   * <p>Note that the query language is case-sensitive.</p>
   *
   * see: {@link Session#createQuery(String, Class)}
   *
   * @param criteria    The query criteria written in the object-oriented query language.
   * @param entityClass The class representing the type of entities or objects that the query will return.
   * @param <T>         The type of entities or objects that the query will return.
   *
   * @return A Query instance for executing the custom query.
   *
   * @throws BibernateBqlException If there is an issue with the provided query criteria.
   */
  <T> Query<T> createQuery(String criteria, Class<T> entityClass);

  <T> void persist(T entity);

  <T> T getReference(Class<T> entityClass, Object id);

  <T> T merge(T entity);

  void detach(Object entity);

  void remove(Object entity);

  void flush();

  void close();

}
