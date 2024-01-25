package com.bibernate.hoverla.exceptions;

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
