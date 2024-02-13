package com.bibernate.hoverla.session.dirtycheck;

import java.util.List;
import java.util.Map;

import com.bibernate.hoverla.metamodel.EntityMapping;
import com.bibernate.hoverla.session.cache.EntityEntry;

/**
 * Performs dirty check and verifies if an {@link EntityEntry entity entry} is dirty. The entity entry is dirty if any of its field has been updated.
 * The comparison is done by comparing the current entity {@link EntityEntry#getEntity() snapshot} with the previous {@link EntityEntry#getSnapshot() snapshot}.
 */
// todo Zhenya Savonenko: add JavaDoc that Ivan posted in PR's comments
public interface DirtyCheckService {

  /**
   * Looks for dirty entities in the given <code>persistenceContextMap</code>.
   *
   * @param persistenceContextMap first-level cache map
   *
   * @return entities that should be updated in a database
   */
  List<Object> findDirtyEntities();

  /**
   * Looks for updated fields in the given <code>entityEntry</code>.
   *
   * @param entityEntry entity entry
   *
   * @return fields that should be updated in a database
   */
  <T> List<DirtyFieldMapping<Object>> getUpdatedFields(T entity);

//  Map<String, Object> getSnapshot(EntityMapping entityMapping, Object entity);
  Object[] getSnapshot(EntityMapping entityMapping, Object entity);

}
