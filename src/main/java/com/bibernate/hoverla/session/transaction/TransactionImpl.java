package com.bibernate.hoverla.session.transaction;

import java.sql.SQLException;

import com.bibernate.hoverla.exceptions.BibernateSqlException;
import com.bibernate.hoverla.exceptions.BibernateTransactionException;
import com.bibernate.hoverla.session.SessionImplementor;

import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of the {@link Transaction} interface. This class is responsible for
 * managing transactions within a session. It provides methods to begin, commit, and
 * rollback transactions, as well as checking the status of the transaction.
 */
@Slf4j
public class TransactionImpl implements Transaction {

  private final SessionImplementor sessionImplementor;

  private boolean isActive;

  public TransactionImpl(SessionImplementor sessionImplementor) {
    this.sessionImplementor = sessionImplementor;
  }
  /**
   * Begins a new transaction.
   *
   * @return The current TransactionImpl instance.
   * @throws BibernateTransactionException If a transaction is already active.
   * @throws BibernateSqlException         If an SQLException occurs during transaction start.
   */
  @Override
  public Transaction beginTransaction() {
    try {
      if (isActive) {
        throw new BibernateTransactionException("Can't begin already active transaction");
      }
      this.sessionImplementor.getConnection().setAutoCommit(false);
      this.isActive = true;
    } catch (SQLException exception) {
      throw new BibernateSqlException("Exception during commit start transaction.", exception);
    }
    return this;
  }
  /**
   * Commits the current transaction.
   *
   * @return The current TransactionImpl instance.
   * @throws BibernateTransactionException If no transaction is active.
   * @throws BibernateSqlException         If an SQLException occurs during commit.
   */
  @Override
  public Transaction commit() {
    try {
      if (!isActive) {
        throw new BibernateTransactionException("Can't commit non active transaction");
      }
      this.sessionImplementor.flush();
      this.sessionImplementor.getConnection().commit();
      this.isActive = false;
    } catch (SQLException exception) {
      throw new BibernateSqlException("Exception during commit current transaction.", exception);
    }
    return this;
  }
  /**
   * Rolls back the current transaction.
   *
   * @return The current TransactionImpl instance.
   * @throws BibernateTransactionException If no transaction is active.
   * @throws BibernateSqlException         If an SQLException occurs during rollback.
   */
  @Override
  public Transaction rollback() {
    try {
      if (!isActive) {
        throw new BibernateTransactionException("Can't rollback non active transaction");
      }
      this.sessionImplementor.invalidateCaches();
      this.sessionImplementor.getConnection().rollback();
      this.isActive = false;
    } catch (SQLException exception) {
      throw new BibernateSqlException("Exception during rollback current transaction.", exception);
    }
    return this;
  }
  /**
   * Checks if the transaction is active.
   *
   * @return True if the transaction is active, false otherwise.
   */
  @Override
  public boolean isActive() {
    return this.isActive;
  }

}
