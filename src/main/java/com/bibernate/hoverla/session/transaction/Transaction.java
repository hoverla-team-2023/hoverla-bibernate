package com.bibernate.hoverla.session.transaction;

public interface Transaction {
  Transaction beginTransaction();

  Transaction commit();

  Transaction rollback();
  boolean isActive();
}
