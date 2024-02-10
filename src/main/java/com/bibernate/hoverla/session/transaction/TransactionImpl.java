package com.bibernate.hoverla.session.transaction;

import java.sql.SQLException;

import com.bibernate.hoverla.exceptions.BibernateSqlException;
import com.bibernate.hoverla.exceptions.BibernateTransactionException;
import com.bibernate.hoverla.session.SessionImplementor;

public class TransactionImpl implements Transaction {

  private final SessionImplementor sessionImplementor;

  private boolean isActive;

  public TransactionImpl(SessionImplementor sessionImplementor) {
    this.sessionImplementor = sessionImplementor;
  }

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

  @Override
  public boolean isActive() {
    return this.isActive;
  }

}
