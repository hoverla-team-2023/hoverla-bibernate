package com.bibernate.hoverla.metamodel.scan;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;

import com.bibernate.hoverla.annotations.Column;
import com.bibernate.hoverla.annotations.Entity;
import com.bibernate.hoverla.annotations.Id;
import com.bibernate.hoverla.annotations.Table;
import com.bibernate.hoverla.exceptions.InvalidEntityDeclarationException;
import com.bibernate.hoverla.jdbc.types.BibernateJdbcType;
import com.bibernate.hoverla.jdbc.types.provider.JdbcTypeProvider;
import com.bibernate.hoverla.metamodel.EntityMapping;
import com.bibernate.hoverla.metamodel.FieldMapping;
import com.bibernate.hoverla.metamodel.Metamodel;

import lombok.RequiredArgsConstructor;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import static com.bibernate.hoverla.utils.EntityUtils.toSnakeCase;

/**
 * Scanner that constructs a {@link Metamodel} from entities based on a package name or a list of entity classes.
 * <br/>
 * A class is considered an entity if it has {@link Entity} annotation on it, and one field annotated with {@link Id}.
 * Specifying {@link Id} on multiple fields is not supported.
 *
 * @see Entity
 * @see Metamodel
 */
@RequiredArgsConstructor
public class MetamodelScanner {

  private final JdbcTypeProvider jdbcTypeProvider;

  /**
   * Scan the provided package and return {@link Metamodel} that describes the entities
   *
   * @param packageName package name
   *
   * @return metadata describing entities in the given package
   */
  public Metamodel scanPackage(String packageName) {
    var reflections = new Reflections(packageName);

    Class<?>[] entities = reflections.getTypesAnnotatedWith(Entity.class)
      .toArray(new Class<?>[0]);
    return scanEntities(entities);
  }

  /**
   * Construct a new {@link Metamodel} from the provided classes
   */
  public Metamodel scanEntities(Class<?>... entityClasses) {
    Map<Class<?>, EntityMapping> entityMappings = Arrays.stream(entityClasses)
      .collect(toMap(identity(), this::scanEntity));

    return new Metamodel(entityMappings);
  }

  private EntityMapping scanEntity(Class<?> entityClass) {
    var entityMapping = new EntityMapping(entityClass, resolveTableName(entityClass));

    Field[] declaredFields = entityClass.getDeclaredFields();
    for (var field : declaredFields) {
      FieldMapping<?> fieldMapping = scanField(field);
      entityMapping.addFieldMapping(field.getName(), fieldMapping);
    }

    validatePrimaryKey(entityClass, entityMapping.getFieldMappingMap().values());

    return entityMapping;
  }

  private void validatePrimaryKey(Class<?> entityClass, Collection<FieldMapping<?>> fieldMappings) {
    long primaryKeyFieldsCount = fieldMappings.stream()
      .filter(FieldMapping::isPrimaryKey)
      .count();

    if (primaryKeyFieldsCount == 0) {
      throw new InvalidEntityDeclarationException(String.format(
        "Entity '%s' has no primary key defined. Please define one with @Id annotation",
        entityClass.getName()
      ));
    }

    if (primaryKeyFieldsCount > 1) {
      throw new InvalidEntityDeclarationException(String.format(
        "Entity '%s' has multiple primary keys defined. Please define only one field with @Id annotation",
        entityClass.getName()
      ));
    }
  }

  private String resolveTableName(Class<?> entityClass) {
    return Optional.ofNullable(entityClass.getAnnotation(Table.class))
      .map(Table::value)
      .orElseGet(() -> toSnakeCase(entityClass.getSimpleName()));
  }

  private <T> FieldMapping<T> scanField(Field field) {
    return FieldMapping.<T>builder()
      .columnName(resolveColumnName(field))
      .jdbcType(resolveJdbcType(field))
      .fieldType(resolveFieldType(field))
      .fieldName(field.getName())
      .isInsertable(resolveColumnProperty(field, Column::insertable, true, false))
      .isUpdatable(resolveColumnProperty(field, Column::updatable, true, false))
      .isNullable(resolveColumnProperty(field, Column::nullable, true, false))
      .isUnique(resolveColumnProperty(field, Column::unique, false, true))
      .isPrimaryKey(field.isAnnotationPresent(Id.class))
      .build();
  }

  private String resolveColumnName(Field field) {
    return resolveColumnProperty(field, Column::name)
      .filter(StringUtils::isNotBlank)
      .orElseGet(() -> toSnakeCase(field.getName()));
  }

  private <T> T resolveColumnProperty(Field field, Function<Column, T> columnMapper, T defaultValue, T defaultValueForPrimaryKey) {
    return resolveColumnProperty(field, columnMapper)
      .orElseGet(() -> field.isAnnotationPresent(Id.class) ? defaultValueForPrimaryKey : defaultValue);
  }

  private <T> Optional<T> resolveColumnProperty(Field field, Function<Column, T> columnMapper) {
    return Optional.ofNullable(field.getAnnotation(Column.class))
      .map(columnMapper);
  }

  private <T> BibernateJdbcType<? super T> resolveJdbcType(Field field) {
    @SuppressWarnings("unchecked")
    Class<? extends BibernateJdbcType<T>> jdbcTypeClass = (Class<? extends BibernateJdbcType<T>>)
      Optional.ofNullable(field.getAnnotation(com.bibernate.hoverla.annotations.JdbcType.class))
        .map(com.bibernate.hoverla.annotations.JdbcType::value)
        .orElse(null);

    Class<T> fieldType = resolveFieldType(field);

    return jdbcTypeProvider.getInstance(jdbcTypeClass, fieldType);
  }

  @SuppressWarnings("unchecked")
  private <T> Class<T> resolveFieldType(Field field) {
    return (Class<T>) field.getType();
  }

}
