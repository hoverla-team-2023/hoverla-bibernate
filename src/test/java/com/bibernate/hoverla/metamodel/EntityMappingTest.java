package com.bibernate.hoverla.metamodel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    var result = entityMapping.getPrimaryKeyMappings();

    assertTrue(result.isPresent());
    assertEquals(primaryKeyMapping, result.get());
  }

  @Test
  void getPrimaryKeyMappings_NoPrimaryKey_ReturnEmptyOptional() {
    FieldMapping<Object> fieldMapping = FieldMapping.builder().build();

    entityMapping.getFieldMappingMap().put("field", fieldMapping);

    var result = entityMapping.getPrimaryKeyMappings();

    assertTrue(result.isEmpty());
  }

  @Test
  void addFieldMapping() {
    FieldMapping<Object> fieldMapping = FieldMapping.builder().build();
    entityMapping.addFieldMapping("field", fieldMapping);

    assertEquals(fieldMapping, entityMapping.getFieldMappingMap().get("field"));
  }

}
