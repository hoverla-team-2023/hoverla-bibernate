package com.bibernate.hoverla.session.transaction;

/**
 * Represents a transaction in the Bibernate framework.
 * A transaction allows operations to be grouped together as a single unit of work that can be committed or rolled back.
 */
public interface Transaction {

  /**
   * Begins a new transaction.
   *
   * @return The transaction instance.
   */
  Transaction beginTransaction();

  /**
   * Commits the current transaction.
   *
   * @return The transaction instance.
   */
  Transaction commit();

  /**
   * Rolls back the current transaction.
   *
   * @return The transaction instance.
   */
  Transaction rollback();

  /**
   * Checks if the transaction is currently active.
   *
   * @return True if the transaction is active, false otherwise.
   */
  boolean isActive();

}
