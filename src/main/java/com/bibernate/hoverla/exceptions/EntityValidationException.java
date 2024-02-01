package com.bibernate.hoverla.exceptions;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EntityValidationException extends BibernateException {
  public EntityValidationException(String message) {
    super(message);
  }
}