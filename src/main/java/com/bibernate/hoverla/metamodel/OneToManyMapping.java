package com.bibernate.hoverla.metamodel;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OneToManyMapping {

  private String mappedBy;
  private Class<?> collectionType;

}
