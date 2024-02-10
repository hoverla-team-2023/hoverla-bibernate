package com.bibernate.hoverla.utils;

import java.util.function.Consumer;
import java.util.function.Function;

import com.bibernate.hoverla.exceptions.BibernateException;
import com.bibernate.hoverla.session.transaction.Transaction;

public class TransactionManagementUtils {

  public static <S> void manageTransaction(S session, Transaction transaction, Consumer<S> consumer) {
    try {
      consumer.accept(session);
      commit(transaction);
    } catch (RuntimeException exception) {
      rollback(transaction, exception);
      throw exception;
    }
  }

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

  private static void rollback(Transaction transaction, RuntimeException exception) {
    if (transaction.isActive()) {
      try {
        transaction.rollback();
      } catch (RuntimeException e) {
        exception.addSuppressed(e);
      }
    }
  }

  private static void commit(Transaction transaction) {
    if (!transaction.isActive()) {
      throw new BibernateException(
        "Execution of action caused managed transaction to be completed");
    }
    transaction.commit();
  }

}
