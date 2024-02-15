package com.bibernate.hoverla.exceptions;

/**
 * Exception thrown when an error occurs in Bibernate's SQL operations.
 */
public class BibernateSqlException extends BibernateException {

  public BibernateSqlException(String message, Throwable cause) {
    super(message, cause);
  }

  public BibernateSqlException(String message) {
    super(message);
  }

}
