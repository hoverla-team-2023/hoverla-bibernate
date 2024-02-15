package com.bibernate.hoverla.exceptions;

/**
 * Exception thrown when a required parameter is missing in Bibernate's BQL (Bibernate Query Language) operations.
 * <p>
 * Extends {@link BibernateBqlException}.
 * </p>
 */
public class BibernateBqlMissingParameterException extends BibernateBqlException {

  public BibernateBqlMissingParameterException(String message) {
    super(message);
  }

}
