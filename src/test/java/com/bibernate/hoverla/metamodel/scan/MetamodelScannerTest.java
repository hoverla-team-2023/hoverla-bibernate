package com.bibernate.hoverla.metamodel.scan;

import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reflections.Reflections;

import com.bibernate.hoverla.annotations.Column;
import com.bibernate.hoverla.annotations.Entity;
import com.bibernate.hoverla.annotations.Id;
import com.bibernate.hoverla.annotations.JdbcType;
import com.bibernate.hoverla.annotations.Table;
import com.bibernate.hoverla.exceptions.InvalidEntityDeclarationException;
import com.bibernate.hoverla.jdbc.types.BibernateJdbcType;
import com.bibernate.hoverla.jdbc.types.PostgreSqlJdbcEnumType;
import com.bibernate.hoverla.jdbc.types.provider.JdbcTypeProvider;
import com.bibernate.hoverla.metamodel.EntityMapping;
import com.bibernate.hoverla.metamodel.FieldMapping;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.AdditionalMatchers.or;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MetamodelScannerTest {

  @InjectMocks
  private MetamodelScanner scanner;
  @Mock
  private JdbcTypeProvider jdbcTypeProvider;

  @Test
  void scanPackage() {
    try (var ignored = mockConstruction(
      Reflections.class,
      (mock, context) -> when(mock.getTypesAnnotatedWith(Entity.class)).thenReturn(Set.of(TestEntity.class)))
    ) {
      when(jdbcTypeProvider.getInstance(isNull(), or(eq(int.class), eq(Integer.class)))).thenReturn(null);

      var metamodel = scanner.scanPackage("com.bibernate.hoverla.metamodel.scan");

      Map<Class<?>, EntityMapping> entityMap = metamodel.getEntityMappingMap();
      assertFalse(entityMap.isEmpty());
      var testEntityMapping = entityMap.get(TestEntity.class);
      assertNotNull(testEntityMapping);
      assertEquals(TestEntity.class, testEntityMapping.getEntityClass());
      assertEquals("test_entity", testEntityMapping.getTableName());

      Map<String, FieldMapping<?>> testEntityFieldMappingMap = testEntityMapping.getFieldMappingMap();
      FieldMapping<?> id = testEntityFieldMappingMap.get("id");
      assertFieldMapping(id, "id", int.class, "id", false, false, false, true, true);

      FieldMapping<?> firstName = testEntityFieldMappingMap.get("firstName");
      assertFieldMapping(firstName, "first_name", String.class, "firstName", false, true, true, false, false);

      FieldMapping<?> lastName = testEntityFieldMappingMap.get("lastName");
      assertFieldMapping(lastName, "last_name", String.class, "lastName", true, false, true, false, false);

      FieldMapping<?> email = testEntityFieldMappingMap.get("email");
      assertFieldMapping(email, "email", String.class, "email", true, true, false, true, false);
    }
  }

  @Test
  void scanPackage_invalidEntity_throwInvalidEntityDeclarationException() {
    Class<TestEntityNoPrimaryKey> entityClass = TestEntityNoPrimaryKey.class;
    String expectedMessage = String.format(
      "Entity '%s' has no primary key defined. Please define one with @Id annotation",
      entityClass.getName()
    );

    try (var ignored = mockConstruction(
      Reflections.class,
      (mock, context) -> when(mock.getTypesAnnotatedWith(Entity.class)).thenReturn(Set.of(entityClass)))
    ) {
      var resultException = assertThrows(InvalidEntityDeclarationException.class, () -> scanner.scanPackage("com.bibernate.hoverla.metamodel.scan"));
      assertEquals(expectedMessage, resultException.getMessage());
    }
  }

  @Test
  void scanEntityWithJdbcType(@Mock BibernateJdbcType<?> jdbcType) {
    Class enumJdbcType = PostgreSqlJdbcEnumType.class;
    Class testEnumClass = TestEntityWithJdbcType.TestEnum.class;

    when(
      jdbcTypeProvider.getInstance(
        or(isNull(), eq(enumJdbcType)),
        or(eq(int.class), eq(testEnumClass)))
    ).thenReturn(null, jdbcType);

    var metamodel = scanner.scanEntities(TestEntityWithJdbcType.class);
    Map<Class<?>, EntityMapping> entityMap = metamodel.getEntityMappingMap();

    assertFalse(entityMap.isEmpty());
    var entityMapping = entityMap.get(TestEntityWithJdbcType.class);
    assertNotNull(entityMapping);
    assertEquals(TestEntityWithJdbcType.class, entityMapping.getEntityClass());
    assertEquals("test_entity_with_jdbc_type", entityMapping.getTableName());

    Map<String, FieldMapping<?>> fieldMappingMap = entityMapping.getFieldMappingMap();
    FieldMapping<?> id = fieldMappingMap.get("id");
    assertFieldMapping(id, "id", int.class, "id", false, false, false, true, true);

    FieldMapping<?> enumeration = fieldMappingMap.get("enumeration");
    assertFieldMapping(enumeration, jdbcType, "enumeration_column", TestEntityWithJdbcType.TestEnum.class, "enumeration", true, false, false, false, false);
  }

  private void assertFieldMapping(
    FieldMapping<?> fieldMapping,
    String columnName,
    Class<?> fieldType,
    String fieldName,
    boolean isInsertable,
    boolean isUpdatable,
    boolean isNullable,
    boolean isUnique,
    boolean isPrimaryKey
  ) {
    assertFieldMapping(fieldMapping, null, columnName, fieldType, fieldName, isInsertable, isUpdatable, isNullable, isUnique, isPrimaryKey);
  }

  private void assertFieldMapping(
    FieldMapping<?> fieldMapping,
    BibernateJdbcType<?> jdbcType,
    String columnName,
    Class<?> fieldType,
    String fieldName,
    boolean isInsertable,
    boolean isUpdatable,
    boolean isNullable,
    boolean isUnique,
    boolean isPrimaryKey
  ) {
    assertEquals(columnName, fieldMapping.getColumnName());
    assertEquals(jdbcType, fieldMapping.getJdbcType());
    assertEquals(fieldType, fieldMapping.getFieldType());
    assertEquals(fieldName, fieldMapping.getFieldName());
    assertEquals(isInsertable, fieldMapping.isInsertable());
    assertEquals(isUpdatable, fieldMapping.isUpdatable());
    assertEquals(isNullable, fieldMapping.isNullable());
    assertEquals(isUnique, fieldMapping.isUnique());
    assertEquals(isPrimaryKey, fieldMapping.isPrimaryKey());
  }

  @Test
  void scanEntity_invalidEntity_throwInvalidEntityDeclarationException() {
    Class<TestEntityInvalidPrimaryKey> entityClass = TestEntityInvalidPrimaryKey.class;
    String expectedMessage = String.format(
      "Entity '%s' has multiple primary keys defined. Please define only one field with @Id annotation",
      entityClass.getName()
    );

    var resultException = assertThrows(InvalidEntityDeclarationException.class, () -> scanner.scanEntities(entityClass));
    assertEquals(expectedMessage, resultException.getMessage());
  }

  @Entity
  private static class TestEntity {

    @Id
    private int id;
    @Column(name = "first_name", insertable = false)
    private String firstName;
    @Column(name = "last_name", updatable = false)
    private String lastName;
    @Column(nullable = false, unique = true)
    private String email;

  }

  @Entity
  @Table("test_entity_with_jdbc_type")
  private static class TestEntityWithJdbcType {

    @Id
    private int id;
    @JdbcType(PostgreSqlJdbcEnumType.class)
    @Column(name = "enumeration_column", nullable = false, updatable = false)
    private TestEnum enumeration;

    private enum TestEnum {
    }

  }

  @Entity
  private static class TestEntityNoPrimaryKey {

    private int id;

  }

  @Entity
  private static class TestEntityInvalidPrimaryKey {

    @Id
    private int key;
    @Id
    private long secondKey;

  }

}
