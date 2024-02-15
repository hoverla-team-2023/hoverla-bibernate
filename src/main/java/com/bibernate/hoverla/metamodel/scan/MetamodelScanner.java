package com.bibernate.hoverla.metamodel.scan;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
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
import com.bibernate.hoverla.annotations.IdentityGeneratedValue;
import com.bibernate.hoverla.annotations.ManyToOne;
import com.bibernate.hoverla.annotations.OneToMany;
import com.bibernate.hoverla.annotations.OptimisticLock;
import com.bibernate.hoverla.annotations.SequenceGeneratedValue;
import com.bibernate.hoverla.annotations.Table;
import com.bibernate.hoverla.annotations.Transient;
import com.bibernate.hoverla.exceptions.InvalidEntityDeclarationException;
import com.bibernate.hoverla.generator.SequenceGeneratorImpl;
import com.bibernate.hoverla.jdbc.types.BibernateJdbcType;
import com.bibernate.hoverla.jdbc.types.provider.JdbcTypeProvider;
import com.bibernate.hoverla.metamodel.EntityMapping;
import com.bibernate.hoverla.metamodel.FieldMapping;
import com.bibernate.hoverla.metamodel.IdGeneratorStrategy;
import com.bibernate.hoverla.metamodel.Metamodel;
import com.bibernate.hoverla.metamodel.OneToManyMapping;
import com.bibernate.hoverla.metamodel.UnsavedValueStrategy;

import lombok.RequiredArgsConstructor;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import static org.apache.commons.lang3.ClassUtils.isPrimitiveOrWrapper;
import static org.apache.commons.lang3.ClassUtils.primitiveToWrapper;

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
      if (field.isAnnotationPresent(Transient.class)) {
        continue;
      }
      FieldMapping<?> fieldMapping = scanField(field);
      entityMapping.addFieldMapping(field.getName(), fieldMapping);
    }

    validatePrimaryKey(entityClass, entityMapping.getFieldNameMappingMap().values());
    validateOptimisticLock(entityClass, entityMapping.getFieldNameMappingMap().values());

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

  private void validateOptimisticLock(Class<?> entityClass, Collection<FieldMapping<?>> fieldMappings) {
    var optimisticLockFields = fieldMappings.stream()
      .filter(FieldMapping::isOptimisticLock)
      .toList();

    if (!optimisticLockFields.isEmpty()) {
      if (optimisticLockFields.size() > 1) {
        throw new InvalidEntityDeclarationException(String.format(
          "Entity '%s' has multiple optimistic lock fields defined. Please define at most one field with @OptimisticLock annotation",
          entityClass.getName()
        ));
      }

      FieldMapping<?> optimisticLock = optimisticLockFields.getFirst();
      validateOptimisticLockType(entityClass, optimisticLock);
    }
  }

  private void validateOptimisticLockType(Class<?> entityClass, FieldMapping<?> fieldMapping) {
    Class<?> fieldType = fieldMapping.getFieldType();
    if (isPrimitiveOrWrapper(fieldType)) {
      Class<?> wrapperType = primitiveToWrapper(fieldType);
      if (Integer.class.isAssignableFrom(wrapperType) || Long.class.isAssignableFrom(wrapperType)) {
        return;
      }
    }

    throw new InvalidEntityDeclarationException(String.format(
      "Entity '%s' has an optimistic lock field '%s' of type '%s' which is not supported. Please use Integer or Long",
      entityClass.getName(), fieldMapping.getFieldName(), fieldType
    ));
  }

  private String resolveTableName(Class<?> entityClass) {
    return Optional.ofNullable(entityClass.getAnnotation(Table.class))
      .map(Table::value)
      .orElseGet(() -> toSnakeCase(entityClass.getSimpleName()));
  }

  private <T> FieldMapping<T> scanField(Field field) {
    FieldMapping<T> fieldMapping = FieldMapping.<T>builder()
      .columnName(resolveColumnName(field))
      .jdbcType(resolveJdbcType(field))
      .fieldType(resolveFieldType(field))
      .fieldName(field.getName())
      .isInsertable(!field.isAnnotationPresent(OneToMany.class) && resolveColumnProperty(field, Column::insertable, true, false))
      .isUpdatable(!field.isAnnotationPresent(OneToMany.class) && resolveColumnProperty(field, Column::updatable, true, false))
      .isNullable(resolveColumnProperty(field, Column::nullable, true, false))
      .isUnique(resolveColumnProperty(field, Column::unique, false, true))
      .isPrimaryKey(field.isAnnotationPresent(Id.class))
      .isManyToOne(field.isAnnotationPresent(ManyToOne.class))
      .idGeneratorStrategy(resolveIdGenerationStrategy(field))
      .oneToManyMapping(resolveOneToManyMapping(field))
      .isOneToMany(field.isAnnotationPresent(OneToMany.class))
      .isOptimisticLock(field.isAnnotationPresent(OptimisticLock.class))
      .build();
    return fieldMapping;
  }

  private OneToManyMapping resolveOneToManyMapping(Field field) {
    OneToMany annotation = field.getAnnotation(OneToMany.class);
    if (annotation == null) {
      return null;
    }
    var parameterizedType = (ParameterizedType) field.getGenericType();
    var typeArguments = parameterizedType.getActualTypeArguments();
    var actualTypeArgument = typeArguments[0];
    var relatedEntityType = (Class<?>) actualTypeArgument;
    return OneToManyMapping.builder()
      .mappedBy(annotation.mappedBy())
      .collectionType(relatedEntityType)
      .build();
  }

  private IdGeneratorStrategy resolveIdGenerationStrategy(Field field) {
    if (!field.isAnnotationPresent(Id.class)) {
      return null;
    }

    if (field.isAnnotationPresent(SequenceGeneratedValue.class)) {
      return getSequenceGeneratedStrategy(field);
    }

    if (field.isAnnotationPresent(IdentityGeneratedValue.class)) {
      return getIdenityGeneratedStrategy();
    }

    return defaultIdGeneratedStrategy();

  }

  private IdGeneratorStrategy getSequenceGeneratedStrategy(Field field) {
    SequenceGeneratedValue sequence = field.getAnnotation(SequenceGeneratedValue.class);
    return IdGeneratorStrategy.builder()
      .isIdentityGenerated(false)
      .generator(new SequenceGeneratorImpl(sequence.sequenceName(), sequence.allocationSize()))
      .unsavedValueStrategy(UnsavedValueStrategy.NULL)
      .build();
  }

  private IdGeneratorStrategy getIdenityGeneratedStrategy() {
    return IdGeneratorStrategy.builder()
      .unsavedValueStrategy(UnsavedValueStrategy.NULL)
      .isIdentityGenerated(true)
      .build();
  }

  private IdGeneratorStrategy defaultIdGeneratedStrategy() {
    return IdGeneratorStrategy.builder()
      .unsavedValueStrategy(UnsavedValueStrategy.ALL)
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
