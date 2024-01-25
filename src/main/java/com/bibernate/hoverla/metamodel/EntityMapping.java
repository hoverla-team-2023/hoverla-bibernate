package com.bibernate.hoverla.metamodel;

import java.util.LinkedHashMap;
import java.util.Map;

public class EntityMapping {

  private final Class<?> entityClass;
  private final String tableName;

  //  Use LinkedHashMap to preserve column order.
  private final Map<String, FieldMapping<?>> fieldMappingMap = new LinkedHashMap<>();

  public EntityMapping(Class<?> entityClass) {
    // todo: convert the class name to snake-case format (e.g. from Bookmarks to bookmarks, from AuthorBooks to author-books)
    this(entityClass, entityClass.getSimpleName());
  }

  public EntityMapping(Class<?> entityClass, String tableName) {
    this.entityClass = entityClass;
    this.tableName = tableName;
  }

}
