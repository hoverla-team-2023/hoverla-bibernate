package com.bibernate.hoverla.exceptions;

/**
 * Exception thrown when an invalid parameter is encountered in Bibernate's BQL (Bibernate Query Language) operations.
 * <p>
 * Extends {@link BibernateBqlException}.
 * </p>
 */
public class BibernateBqlInvalidParameterException extends BibernateBqlException {

  public BibernateBqlInvalidParameterException(String message) {
    super(message);
  }

}
