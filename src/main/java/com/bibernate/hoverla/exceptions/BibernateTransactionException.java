package com.bibernate.hoverla.exceptions;

public class BibernateTransactionException extends BibernateException {

  public BibernateTransactionException(String message) {
    super(message);
  }

  public BibernateTransactionException(String message, Throwable cause) {
    super(message, cause);
  }

}
