package com.bibernate.hoverla.metamodel;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MetamodelTest {

  @Test
  void merge() {
    EntityMapping mapping1 = new EntityMapping(Entity1.class);
    EntityMapping mapping2 = new EntityMapping(Entity2.class);
    EntityMapping mapping3 = new EntityMapping(Entity3.class);

    Map<Class<?>, EntityMapping> entityMapping1 = new HashMap<>();
    entityMapping1.put(Entity1.class, mapping1);
    entityMapping1.put(Entity2.class, mapping2);
    var current = new Metamodel(entityMapping1);

    var newMetamodel = new Metamodel(Map.of(Entity3.class, mapping3));

    current.merge(newMetamodel);

    assertEquals(Map.of(
      Entity1.class, mapping1,
      Entity2.class, mapping2,
      Entity3.class, mapping3
    ), current.getEntityMappingMap());
  }

  private static class Entity1 {}

  private static class Entity2 {}

  private static class Entity3 {}

}
