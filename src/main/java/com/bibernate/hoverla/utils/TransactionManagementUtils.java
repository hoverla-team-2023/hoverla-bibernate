package com.bibernate.hoverla.utils;

import java.util.function.Consumer;
import java.util.function.Function;

import com.bibernate.hoverla.exceptions.BibernateException;
import com.bibernate.hoverla.session.transaction.Transaction;
/**
 * This class provides utility methods for managing transactions in a session.
 * It encapsulates the common logic for transaction management, such as committing or rolling back a transaction.
 */
public class TransactionManagementUtils {

  /**
   * This method manages a transaction by executing a consumer function within a transaction.
   * If the consumer function throws a RuntimeException, the transaction is rolled back.
   *
   * @param <S> The type of the session.
   * @param session The session to execute the consumer function within.
   * @param transaction The transaction to manage.
   * @param consumer The consumer function to execute within the transaction.
   */
  public static <S> void manageTransaction(S session, Transaction transaction, Consumer<S> consumer) {
    try {
      consumer.accept(session);
      commit(transaction);
    } catch (RuntimeException exception) {
      rollback(transaction, exception);
      throw exception;
    }
  }
  /**
   * This method manages a transaction by executing a function within a transaction.
   * If the function throws a RuntimeException, the transaction is rolled back.
   *
   * @param <S> The type of the session.
   * @param <R> The type of the result of the function.
   * @param session The session to execute the function within.
   * @param transaction The transaction to manage.
   * @param function The function to execute within the transaction.
   * @return The result of the function.
   */
  public static <S, R> R manageTransaction(S session, Transaction transaction, Function<S, R> function) {
    try {
      R result = function.apply(session);
      commit(transaction);
      return result;
    } catch (RuntimeException exception) {
      rollback(transaction, exception);
      throw exception;
    }
  }
  /**
   * This private method rolls back a transaction and handles any exceptions that occur during the rollback.
   *
   * @param transaction The transaction to roll back.
   * @param exception The exception that caused the rollback.
   */
  private static void rollback(Transaction transaction, RuntimeException exception) {
    if (transaction.isActive()) {
      try {
        transaction.rollback();
      } catch (RuntimeException e) {
        exception.addSuppressed(e);
      }
    }
  }
  /**
   * This private method commits a transaction.
   * If the transaction is not active, it throws a BibernateException.
   *
   * @param transaction The transaction to commit.
   */
  private static void commit(Transaction transaction) {
    if (!transaction.isActive()) {
      throw new BibernateException(
        "Execution of action caused managed transaction to be completed");
    }
    transaction.commit();
  }

}
