package com.bibernate.hoverla.exceptions;

public class ConfigurationException extends BibernateException {
  /**
   * Constructs a ConfigurationException with the specified detail message.
   *
   * @param message the detail message.
   */
  public ConfigurationException(String message) {
    super(message);
  }

  /**
   * Constructs a ConfigurationException with the specified detail message
   * and cause.
   *
   * @param message the detail message.
   * @param cause   the cause (which is saved for later retrieval by the Throwable.getCause() method).
   */
  public ConfigurationException(String message, Throwable cause) {
    super(message, cause);
  }
}
