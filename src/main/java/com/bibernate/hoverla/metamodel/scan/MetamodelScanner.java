package com.bibernate.hoverla.metamodel.scan;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Set;

import org.reflections.Reflections;

import com.bibernate.hoverla.annotations.Column;
import com.bibernate.hoverla.annotations.Entity;
import com.bibernate.hoverla.annotations.Id;
import com.bibernate.hoverla.metamodel.EntityMapping;
import com.bibernate.hoverla.metamodel.FieldMapping;
import com.bibernate.hoverla.metamodel.Metamodel;

/**
 * Scanner that constructs a {@link Metamodel} from entities. A class is considered an entity if it
 * has {@link Entity} annotation on it, and one field annotated with {@link Id}.
 */
public class MetamodelScanner {

  /**
   * Scan all the entities in the current classpath
   * <br/>
   * This method will probably be removed, so please try to leverage {@link #scanPackage(String)} instead.
   *
   * @return metadata describing entities in the current classpath
   */
  public Metamodel scan() {
    return null;
  }

  /**
   * Scan the provided package and return {@link Metamodel} that describes the entities
   *
   * @param packageName package name
   *
   * @return metadata describing entities in the given package
   */
  public Metamodel scanPackage(String packageName) {
    var reflections = new Reflections(packageName);
    Set<Class<?>> entitites = reflections.getTypesAnnotatedWith(Entity.class);
    entitites.stream()
//      .filter(entity -> {
//        Arrays.stream(entity.getDeclaredFields())
//          .filter(field -> field.isAnnotationPresent(Id.class))
//          .
//      })
      .map(this::scanEntity);

    return null;
  }

  private EntityMapping scanEntity(Class<?> entityClass) {
    Arrays.stream(entityClass.getDeclaredFields())
      .map(this::scanField);
  }

  private <T> FieldMapping<T> scanField(Field field) {
    FieldMapping.builder()
      .columnName(field.isAnnotationPresent(Column.class) ?
                   field.getAnnotation(Column.class).value() :
                  )
    return null;
  }

  /**
   * Construct a new {@link Metamodel} from the provided classes
   */
  public Metamodel scanEntities(Class<?>... entityClasses) {
    return null;
  }

}
