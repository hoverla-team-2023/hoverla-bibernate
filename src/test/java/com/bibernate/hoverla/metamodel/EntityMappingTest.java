package com.bibernate.hoverla.metamodel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EntityMappingTest {

  private EntityMapping entityMapping;

  @BeforeEach
  void setUp() {
    entityMapping = new EntityMapping(Object.class);
  }

  @Test
  void getPrimaryKeyMappings() {
    FieldMapping<Object> primaryKeyMapping = FieldMapping.builder().isPrimaryKey(true).build();

    entityMapping.getFieldMappingMap().put("id", primaryKeyMapping);

    var result = entityMapping.getPrimaryKeyMapping();

    assertEquals(primaryKeyMapping, result);
  }

  // @Test
  //todo
  void getPrimaryKeyMappings_NoPrimaryKey_ReturnEmptyOptional() {
    FieldMapping<Object> fieldMapping = FieldMapping.builder().build();

    entityMapping.getFieldMappingMap().put("field", fieldMapping);

    var result = entityMapping.getPrimaryKeyMapping();

    //assertTrue(result.isEmpty());
  }

  @Test
  void addFieldMapping() {
    FieldMapping<Object> fieldMapping = FieldMapping.builder().build();
    entityMapping.addFieldMapping("field", fieldMapping);

    assertEquals(fieldMapping, entityMapping.getFieldMappingMap().get("field"));
  }

}
