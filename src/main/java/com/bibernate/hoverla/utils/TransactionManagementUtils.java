package com.bibernate.hoverla.utils;

import java.util.function.Consumer;
import java.util.function.Function;

import com.bibernate.hoverla.exceptions.BibernateException;
import com.bibernate.hoverla.session.transaction.Transaction;

/**
 * Utility class for managing transactions.
 */
public class TransactionManagementUtils {

  /**
   * Executes the provided consumer within a transaction and commits the transaction.
   * If any runtime exception occurs during the execution, the transaction is rolled back.
   *
   * @param session     The session to be used for the transaction.
   * @param transaction The transaction to be managed.
   * @param consumer    The consumer to be executed within the transaction.
   * @param <S>         The type of the session.
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
   * Executes the provided function within a transaction, commits the transaction, and returns the result of the function.
   * If any runtime exception occurs during the execution, the transaction is rolled back.
   *
   * @param session     The session to be used for the transaction.
   * @param transaction The transaction to be managed.
   * @param function    The function to be executed within the transaction.
   * @param <S>         The type of the session.
   * @param <R>         The type of the result returned by the function.
   *
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
   * Rolls back the transaction if it is active and adds any exception that occurs during rollback as a suppressed exception.
   *
   * @param transaction The transaction to be rolled back.
   * @param exception   The exception that occurred during the transaction execution.
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
   * Commits the transaction if it is active.
   *
   * @param transaction The transaction to be committed.
   */
  private static void commit(Transaction transaction) {
    if (!transaction.isActive()) {
      throw new BibernateException(
        "Execution of action caused managed transaction to be completed");
    }
    transaction.commit();
  }

}
