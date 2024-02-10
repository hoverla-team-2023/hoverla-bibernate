package com.bibernate.hoverla.session;

import java.util.function.Consumer;
import java.util.function.Function;

import static com.bibernate.hoverla.utils.TransactionManagementUtils.manageTransaction;

public interface SessionFactory {

  Session openSession();

  default void inSession(Consumer<Session> action) {
    try (Session session = openSession()) {
      action.accept(session);
    }
  }

  default <R> R fromSession(Function<Session, R> action) {
    try (Session session = openSession()) {
      return action.apply(session);
    }
  }

  default void inTransaction(Consumer<Session> action) {
    inSession(session -> manageTransaction(session, session.getTransaction().beginTransaction(), action));
  }

  default <R> R fromTransaction(Function<Session, R> action) {
    return fromSession(session -> manageTransaction(session, session.getTransaction().beginTransaction(), action));
  }

}
