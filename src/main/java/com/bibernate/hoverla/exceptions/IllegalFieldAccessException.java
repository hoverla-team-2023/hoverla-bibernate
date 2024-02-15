package com.bibernate.hoverla.exceptions;

/**
 * Exception thrown when attempting to access a field illegally in Bibernate.
 */
public class IllegalFieldAccessException extends BibernateException {

  public IllegalFieldAccessException(String message, Throwable cause) {
    super(message, cause);
  }

}
