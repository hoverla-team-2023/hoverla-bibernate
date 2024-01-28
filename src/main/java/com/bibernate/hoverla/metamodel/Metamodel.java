package com.bibernate.hoverla.metamodel;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

@Data
public class Metamodel {

  private final Map<Class<?>, EntityMapping> entityMappingMap = new HashMap<>();

  /**
   * Merge current {@link Metamodel#entityMappingMap} with the provided {@link Metamodel#entityMappingMap}
   *
   * @return current instance of {@link Metamodel} with merged inner info
   */
  public Metamodel merge(Metamodel metamodel) {
    return this;
  }

}
