package com.bibernate.hoverla.metamodel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.bibernate.hoverla.exceptions.InvalidEntityDeclarationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EntityMappingTest {

  private EntityMapping entityMapping;

  @BeforeEach
  void setUp() {
    entityMapping = new EntityMapping(Object.class);
  }

  @Test
  void getPrimaryKeyMappings() {
    FieldMapping<Object> primaryKeyMapping = FieldMapping.builder().isPrimaryKey(true).build();

    entityMapping.getFieldNameMappingMap().put("id", primaryKeyMapping);

    var result = entityMapping.getPrimaryKeyMapping();

    assertEquals(primaryKeyMapping, result);
  }

  @Test
  void getPrimaryKeyMappings_NoPrimaryKey_ThrowInvalidEntityDeclarationException() {
    FieldMapping<Object> fieldMapping = FieldMapping.builder().build();

    entityMapping.getFieldNameMappingMap().put("field", fieldMapping);

    InvalidEntityDeclarationException result = assertThrows(InvalidEntityDeclarationException.class, () -> entityMapping.getPrimaryKeyMapping());

    assertEquals("No primary key id declared", result.getMessage());
  }

  @Test
  void addFieldMapping() {
    FieldMapping<Object> fieldMapping = FieldMapping.builder().build();
    entityMapping.addFieldMapping("field", fieldMapping);

    assertEquals(fieldMapping, entityMapping.getFieldNameMappingMap().get("field"));
  }

}
