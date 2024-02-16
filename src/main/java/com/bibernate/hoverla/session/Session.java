package com.bibernate.hoverla.session;

import com.bibernate.hoverla.exceptions.BibernateBqlException;
import com.bibernate.hoverla.query.Query;
import com.bibernate.hoverla.session.transaction.Transaction;

/**
 * The Session interface in Hibernate is the main way to interact with the database. It manages entities (data objects) within a transaction, allowing you to save, update, or delete them. Here's what you need to know:
 * <p/>
 * - Entities can be new (transient), managed (persistent), or disconnected (detached) from the Session.
 * - Use `persist` to save a new entity, `merge` to update a detached entity, and `remove` to delete an entity.
 * - Changes made to persistent entities are automatically saved to the database when the transaction is committed.
 * - It's important to close the Session when you're done to free up resources.
 * - Sessions are not thread-safe; each thread should have its own Session.
 * - Objects obtained through `getReference` and `find` are the same instance within a Session, ensuring consistency.
 * <p/>
 */
public interface Session extends AutoCloseable {

  /**
   * Finds an entity by its class type and primary key.
   *
   * @param entityClass The class of the entity to find.
   * @param id          The primary key of the entity.
   * @param <T>         The type of the entity.
   *
   * @return The found entity or {@code null} if the entity does not exist.
   */
  <T> T find(Class<T> entityClass, Object id);

  /**
   * Finds an entity by its class type and primary key.
   *
   * @param entityClass The class of the entity to find.
   * @param id          The primary key of the entity.
   * @param <T>         The type of the entity.
   *
   * @return The found entity or {@code null} if the entity does not exist.
   */
  <T> T find(Class<T> entityClass, Object id, LockMode lockMode);

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

  /**
   * Persists a new entity into the database.
   *
   * @param entity The entity to be persisted.
   * @param <T>    The type of the entity.
   */
  <T> void persist(T entity);

  /**
   * Retrieves an entity reference without initializing it. Useful for setting references to entities without
   * requiring a database call.
   *
   * @param entityClass The class of the entity to reference.
   * @param id          The primary key of the entity.
   * @param <T>         The type of the entity.
   *
   * @return A reference to the entity.
   */
  <T> T getReference(Class<T> entityClass, Object id);

  /**
   * Merges the state of the given entity into the current persistence context.
   *
   * @param entity The entity to merge.
   * @param <T>    The type of the entity.
   *
   * @return The managed instance that the state was merged into.
   */
  <T> T merge(T entity);

  /**
   * Detaches an entity from the persistence context, so changes to it will not be synchronized with the database.
   *
   * @param entity The entity to detach.
   */
  void detach(Object entity);

  /**
   * Removes an entity from the database.
   *
   * @param entity The entity to remove.
   */
  void remove(Object entity);

  /**
   * Flushes all pending changes to the database immediately.
   */
  void flush();

  /**
   * Retrieves the current transaction associated with this session.
   *
   * @return The current {@link Transaction}.
   */
  Transaction getTransaction();

  /**
   * Closes the session, releasing all resources associated with it.
   */
  @Override
  void close();

}
