package com.bibernate.hoverla.metamodel;

/**
 * Enumerates the strategies for handling unsaved values in ID generation.
 */
public enum UnsavedValueStrategy {

  /**
   * Represents the strategy where all values are considered unsaved.
   */
  ALL,

  /**
   * Represents the strategy where only null values are considered unsaved.
   */
  NULL
}
