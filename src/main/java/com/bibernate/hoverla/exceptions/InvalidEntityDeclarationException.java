package com.bibernate.hoverla.exceptions;

/**
 * Exception thrown when an invalid entity declaration is encountered.
 */
public class InvalidEntityDeclarationException extends BibernateException {

  public InvalidEntityDeclarationException(String message) {
    super(message);
  }

}
