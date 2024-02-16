package com.bibernate.hoverla.session.transaction;

import java.sql.SQLException;

import com.bibernate.hoverla.exceptions.BibernateSqlException;
import com.bibernate.hoverla.exceptions.BibernateTransactionException;
import com.bibernate.hoverla.session.SessionImplementor;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TransactionImpl implements Transaction {

  private final SessionImplementor sessionImplementor;

  private boolean isActive;

  public TransactionImpl(SessionImplementor sessionImplementor) {
    this.sessionImplementor = sessionImplementor;
  }

  @Override
  public Transaction beginTransaction() {
    log.trace("Starting transaction...");

    try {
      if (isActive) {
        throw new BibernateTransactionException("Can't begin already active transaction");
      }
      this.sessionImplementor.getConnection().setAutoCommit(false);
      this.isActive = true;
      log.debug("Transaction started successfully.");
    } catch (SQLException exception) {
      throw new BibernateSqlException("Exception during commit start transaction.", exception);
    }
    return this;
  }

  @Override
  public Transaction commit() {
    log.trace("Committing transaction...");

    try {
      if (!isActive) {
        throw new BibernateTransactionException("Can't commit non active transaction");
      }
      this.sessionImplementor.flush();
      this.sessionImplementor.getConnection().commit();
      this.isActive = false;

      log.debug("Transaction committed successfully.");

    } catch (SQLException exception) {
      throw new BibernateSqlException("Exception during commit current transaction.", exception);
    }
    return this;
  }

  @Override
  public Transaction rollback() {
    log.debug("Rollback transaction started...");

    try {
      if (!isActive) {
        throw new BibernateTransactionException("Can't rollback non active transaction");
      }
      this.sessionImplementor.invalidateCaches();
      this.sessionImplementor.getConnection().rollback();
      this.isActive = false;

      log.debug("Transaction has been rolled back.");

    } catch (SQLException exception) {
      throw new BibernateSqlException("Exception during rollback current transaction.", exception);
    }
    return this;
  }

  @Override
  public boolean isActive() {
    return this.isActive;
  }

}
