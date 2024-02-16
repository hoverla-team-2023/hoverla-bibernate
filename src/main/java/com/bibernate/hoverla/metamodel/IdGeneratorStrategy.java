package com.bibernate.hoverla.metamodel;

import com.bibernate.hoverla.generator.Generator;

import lombok.Builder;
import lombok.Getter;

/**
 * Represents an ID generator strategy, including the unsaved value strategy, identity generation flag, and generator type.
 */
@Builder
@Getter
public class IdGeneratorStrategy {

  /**
   * The unsaved value strategy used for the ID generation.
   */
  private UnsavedValueStrategy unsavedValueStrategy;

  /**
   * Indicates whether identity generation is enabled.
   */
  private boolean isIdentityGenerated;

  /**
   * The type of generator used for ID generation.
   */
  private Generator generator;

}
