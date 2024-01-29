package com.bibernate.hoverla.exceptions;

/**
 * Base class for exceptions in the Bibernate framework.
 * <p/>
 * All custom exceptions within the Bibernate framework
 * should extend this class to maintain a consistent and structured exception hierarchy.
 */
public class BibernateException extends RuntimeException {

  public BibernateException() {
  }

  public BibernateException(String message) {
    super(message);
  }

  public BibernateException(String message, Throwable cause) {
    super(message, cause);
  }

  public BibernateException(Throwable cause) {
    super(cause);
  }

}
