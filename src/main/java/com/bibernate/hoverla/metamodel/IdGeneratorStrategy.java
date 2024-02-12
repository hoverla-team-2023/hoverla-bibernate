package com.bibernate.hoverla.metamodel;

import com.bibernate.hoverla.generator.Generator;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class IdGeneratorStrategy {

  private UnsavedValueStrategy unsavedValueStrategy;
  private boolean isIdentityGenerated;
  private Generator generator;

}
