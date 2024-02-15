package com.bibernate.hoverla.exceptions;

import com.bibernate.hoverla.annotations.OptimisticLock;

/**
 * Exception indicating a violation of optimistic lock strategy during entity update.
 *
 * @see OptimisticLock
 */
public class OptimisticLockException extends BibernateException {

  public OptimisticLockException(String message) {
    super(message);
  }

}
