package com.bibernate.hoverla.utils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;

import com.bibernate.hoverla.annotations.Id;
import com.bibernate.hoverla.annotations.ManyToOne;
import com.bibernate.hoverla.annotations.OneToMany;
import com.bibernate.hoverla.exceptions.BibernateException;
import com.bibernate.hoverla.exceptions.EntityValidationException;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EntityUtils {

  /**
   * Converts a camel case name to a snake case name with a first letter in the lower case.
   * For example, "firstName" becomes "first_name", "BookAuthors" becomes "book_authors".
   *
   * @param value the name to convert
   *
   * @return the converted name in snake case
   */
  public static String toSnakeCase(String value) {
    if (value == null) {
      return null;
    }
    return value.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
  }

  /**
   * Retrieves the identifier of the given entity object.
   *
   * @param <T>     the type of the entity object
   * @param entity  the entity object
   * @return an Optional containing the identifier of the entity object, or an empty Optional if no identifier is found
   * @throws EntityValidationException  if the entity object does not have a field annotated with @Id
   */
  public static <T> Optional<Object> getId(T entity) {
    return Arrays.stream(entity.getClass().getDeclaredFields())
      .filter(EntityUtils::isIdField)
      .findAny()
      .map(field -> retrieveValueFromField(entity, field))
      .orElseThrow(() -> new EntityValidationException(
        "Entity '%s' does not have a field annotated with @Id " + "(every '@Entity' class must have a field annotated with @Id')"));
  }

  /**
   * Checks if a given field is annotated with {@code Id}.
   *
   * @param field the field to check
   * @return {@code true} if the field is annotated with {@code Id}, {@code false} otherwise
   */
  public static boolean isIdField(Field field) {
    return field.isAnnotationPresent(Id.class);
  }

  /**
   * Retrieves the value from a field in an entity object.
   *
   * @param <T>     the type of the entity object
   * @param entity  the entity object from which to retrieve the value
   * @param field   the field from which to retrieve the value
   * @return an Optional containing the value of the field, or an empty Optional if the value cannot be retrieved
   * @throws BibernateException if there is an error while retrieving the value from the field
   */
  public static <T> Optional<Object> retrieveValueFromField(T entity, Field field) {
    try {
      Object value = null;
      field.setAccessible(true);
      if (isSimpleColumnField(field)) {
        value = field.get(entity);
      } else if (isEntityField(field)) {
        Object relatedEntity = field.get(entity);
        value = getId(relatedEntity);
      }
      return Optional.ofNullable(value);
    } catch (Exception e) {
      throw new BibernateException(String.format("Error while retrieving the value from field '%s' in type '%s'", field.getName(), entity.getClass().getName()),
                                   e);
    }
  }

  /**
   * Checks if a given field is a simple column field.
   *
   * @param field the field to check
   * @return {@code true} if the field is a simple column field, {@code false} otherwise
   */
  public static boolean isSimpleColumnField(Field field) {
    return !isRelationsField(field);
  }

  /**
   * Checks if a given field is a relations field.
   *
   * This method checks if a field in an entity object represents a relations field. A relations field is defined as a field that is either an entity field or an entity collection
   * field.
   * An entity field is a field that is annotated with {@code @ManyToOne}.
   * An entity collection field is a field that is annotated with {@code @OneToMany}.
   *
   * @param field the field to check
   * @return {@code true} if the field is a relations field, {@code false} otherwise
   */
  public static boolean isRelationsField(Field field) {
    return isEntityField(field) || isEntityCollectionField(field);
  }

  /**
   * Checks if a given field is an entity field.
   *
   * @param field the field to check
   * @return {@code true} if the field is an entity field annotated with {@code ManyToOne}, {@code false} otherwise
   */
  public static boolean isEntityField(Field field) {
    return field.isAnnotationPresent(ManyToOne.class);
  }

  /**
   * Checks if a given field is an entity collection field.
   *
   * @param field the field to check
   * @return {@code true} if the field is an entity collection field annotated with {@code OneToMany}, {@code false} otherwise
   */
  public static boolean isEntityCollectionField(Field field) {
    return field.isAnnotationPresent(OneToMany.class);
  }

}
