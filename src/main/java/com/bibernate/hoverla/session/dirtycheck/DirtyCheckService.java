package com.bibernate.hoverla.session.dirtycheck;

import java.util.List;

import com.bibernate.hoverla.metamodel.EntityMapping;
import com.bibernate.hoverla.session.cache.EntityEntry;

/**
 * Performs dirty check and verifies if an {@link EntityEntry entity entry} is dirty. The entity entry is dirty if any of its field has been updated.
 * The comparison is done by comparing the current entity {@link EntityEntry#getEntity() snapshot} with the previous {@link EntityEntry#getSnapshot() snapshot}.
 */
public interface DirtyCheckService {

  /**
   * Finds all dirty entities in the given persistence context map.
   * A dirty entity is an entity that is managed (i.e., it is part of the persistence context),
   * is not read-only, and has been modified since it was last persisted.
   *
   * @return A list of all dirty entities found in the persistence context map.
   */
  List<?> findDirtyEntities();

  /**
   * Retrieves a list of dirty field mappings for the updated fields of an entity.
   *
   * @param entity entity
   *
   * @return A list of DirtyFieldMapping objects for the updated fields.
   */
  <T> List<DirtyFieldMapping<Object>> getUpdatedFields(T entity);

  Object[] getSnapshot(Class<?> entityClass, Object entity);

}
