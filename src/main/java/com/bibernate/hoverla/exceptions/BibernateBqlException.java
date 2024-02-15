package com.bibernate.hoverla.exceptions;
/**
 * Exception thrown when an error occurs in Bibernate's BQL (Bibernate Query Language) operations.
 */
public class BibernateBqlException extends BibernateException {

  public BibernateBqlException(String message) {
    super(message);
  }

}
