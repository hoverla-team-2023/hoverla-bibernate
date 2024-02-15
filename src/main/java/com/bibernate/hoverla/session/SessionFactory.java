package com.bibernate.hoverla.session;

import java.util.function.Consumer;
import java.util.function.Function;

import javax.sql.DataSource;

import static com.bibernate.hoverla.utils.TransactionManagementUtils.manageTransaction;

/**
 * Interface representing a factory for creating sessions in Bibernate.
 */
public interface SessionFactory {

  /**
   * Opens a new session.
   *
   * @return a new session.
   */
  Session openSession();

  /**
   * Retrieves the data source associated with this session factory.
   *
   * @return the data source associated with this session factory.
   */
  DataSource getDataSource();

  /**
   * Executes the specified action within a session.
   *
   * @param action the action to be executed within a session.
   */
  default void inSession(Consumer<Session> action) {
    try (Session session = openSession()) {
      action.accept(session);
    }
  }

  /**
   * Executes the specified action and returns the result within a session.
   *
   * @param action the action to be executed within a session.
   * @param <R>    the type of the result returned by the action.
   *
   * @return the result returned by the action.
   */
  default <R> R fromSession(Function<Session, R> action) {
    try (Session session = openSession()) {
      return action.apply(session);
    }
  }

  /**
   * Executes the specified action within a transaction.
   *
   * @param action the action to be executed within a transaction.
   */
  default void inTransaction(Consumer<Session> action) {
    inSession(session -> manageTransaction(session, session.getTransaction().beginTransaction(), action));
  }

  /**
   * Executes the specified action and returns the result within a transaction.
   *
   * @param action the action to be executed within a transaction.
   * @param <R>    the type of the result returned by the action.
   *
   * @return the result returned by the action.
   */
  default <R> R fromTransaction(Function<Session, R> action) {
    return fromSession(session -> manageTransaction(session, session.getTransaction().beginTransaction(), action));
  }

}
