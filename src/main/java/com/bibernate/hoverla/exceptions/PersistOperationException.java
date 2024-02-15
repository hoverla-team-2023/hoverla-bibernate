package com.bibernate.hoverla.exceptions;

/**
 * Exception thrown when an error occurs during persistence operations in Bibernate.
 */
public class PersistOperationException extends BibernateException {

  public PersistOperationException(String message) {
    super(message);
  }

}
