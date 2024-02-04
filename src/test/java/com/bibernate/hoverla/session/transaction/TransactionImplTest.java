package com.bibernate.hoverla.session.transaction;

import java.sql.Connection;
import java.sql.SQLException;
import org.junit.jupiter.api.Test;

import com.bibernate.hoverla.exceptions.BibernateTransactionException;
import com.bibernate.hoverla.session.SessionImplementor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TransactionImplTest {

  @Test
  void begin() throws SQLException {
    SessionImplementor sessionImplementor = mock(SessionImplementor.class);
    Connection connection = mock(Connection.class);
    when(sessionImplementor.getConnection()).thenReturn(connection);

    TransactionImpl transaction = new TransactionImpl(sessionImplementor);

    assertFalse(transaction.isActive());

    Transaction returned = transaction.beginTransaction();

    assertTrue(transaction.isActive());
    assertTrue(transaction == returned);

    verify(connection).setAutoCommit(false);
  }

  @Test
  void begin_tryToBeginAlreadyBeginnedTransactionShouldThrowException() throws SQLException {
    SessionImplementor sessionImplementor = mock(SessionImplementor.class);
    Connection connection = mock(Connection.class);
    when(sessionImplementor.getConnection()).thenReturn(connection);

    TransactionImpl transaction = new TransactionImpl(sessionImplementor);

    assertFalse(transaction.isActive());
    transaction.beginTransaction();
    assertTrue(transaction.isActive());
    BibernateTransactionException exception = assertThrows(BibernateTransactionException.class, transaction::beginTransaction);

    assertEquals("Can't begin already active transaction", exception.getMessage());

    verify(connection).setAutoCommit(false);
  }

  @Test
  void commit() throws SQLException {
    SessionImplementor sessionImplementor = mock(SessionImplementor.class);
    Connection connection = mock(Connection.class);
    when(sessionImplementor.getConnection()).thenReturn(connection);

    TransactionImpl transaction = new TransactionImpl(sessionImplementor);
    transaction.beginTransaction();

    transaction.commit();

    verify(sessionImplementor).flush();
    verify(connection).commit();
    verify(connection, never()).rollback();
  }

  @Test
  void commit_tryToCommitNonStartedTransactionShouldThrowException() {
    SessionImplementor sessionImplementor = mock(SessionImplementor.class);

    TransactionImpl transaction = new TransactionImpl(sessionImplementor);

    BibernateTransactionException exception = assertThrows(BibernateTransactionException.class, transaction::commit);

    assertEquals("Can't commit non active transaction", exception.getMessage());
  }

  @Test
  void testRollback() throws SQLException {
    SessionImplementor sessionImplementor = mock(SessionImplementor.class);
    Connection connection = mock(Connection.class);
    when(sessionImplementor.getConnection()).thenReturn(connection);

    TransactionImpl transaction = new TransactionImpl(sessionImplementor);
    transaction.beginTransaction();

    transaction.rollback();

    verify(sessionImplementor).invalidateCaches();
    verify(connection).rollback();
    verify(connection, never()).commit();
  }

  @Test
  void testRollbackNonActiveTransaction() {
    SessionImplementor sessionImplementor = mock(SessionImplementor.class);

    TransactionImpl transaction = new TransactionImpl(sessionImplementor);

    BibernateTransactionException exception =
      assertThrows(
        BibernateTransactionException.class, transaction::rollback);

    assert exception.getMessage().equals("Can't rollback non active transaction");
  }

  @Test
  void testIsActive() throws SQLException {
    SessionImplementor sessionImplementor = mock(SessionImplementor.class);
    Connection connection = mock(Connection.class);

    when(sessionImplementor.getConnection()).thenReturn(connection);

    TransactionImpl transaction = new TransactionImpl(sessionImplementor);

    assertFalse(transaction.isActive());

    transaction.beginTransaction();

    assertTrue(transaction.isActive());

    transaction.commit();

    assertFalse(transaction.isActive());
  }


}