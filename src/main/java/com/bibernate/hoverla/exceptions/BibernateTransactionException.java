package com.bibernate.hoverla.exceptions;

/**
 * Exception thrown when an error occurs in Bibernate's transaction handling.
 */
public class BibernateTransactionException extends BibernateException {

  public BibernateTransactionException(String message) {
    super(message);
  }

}
