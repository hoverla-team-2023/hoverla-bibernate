package com.bibernate.hoverla.exceptions;

public class BibernateSqlException extends BibernateException {

  public BibernateSqlException() {
  }

  public BibernateSqlException(String message) {
    super(message);
  }

  public BibernateSqlException(String message, Throwable cause) {
    super(message, cause);
  }

  public BibernateSqlException(Throwable cause) {
    super(cause);
  }

}
