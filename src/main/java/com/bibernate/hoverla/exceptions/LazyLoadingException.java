package com.bibernate.hoverla.exceptions;

/**
 * Represents an exception that occurs during lazy loading in the Bibernate framework.
 */
public class LazyLoadingException extends BibernateException {

  public LazyLoadingException(String message) {
    super(message);
  }

}
