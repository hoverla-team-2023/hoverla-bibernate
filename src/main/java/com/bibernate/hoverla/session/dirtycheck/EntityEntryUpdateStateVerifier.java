package com.bibernate.hoverla.session.dirtycheck;

import java.util.Map;

import com.bibernate.hoverla.metamodel.EntityMapping;
import com.bibernate.hoverla.session.cache.EntityEntry;
import com.bibernate.hoverla.session.cache.EntityKey;

/**
 * Performs dirty check and verifies if an {@link EntityEntry entity entry} is dirty. The entity entry is dirty if any of its field has been updated.
 * The comparison is done by comparing the current entity {@link EntityEntry#getEntity() snapshot} with the previous {@link EntityEntry#getSnapshot() snapshot}.
 */
public interface EntityEntryUpdateStateVerifier {

  /**
   * Looks for dirty entities in the given <code>persistenceContextMap</code>.
   *
   * @param persistenceContextMap first-level cache map
   *
   * @return entities that should be updated in a database
   */
  Object[] findDirtyEntities(Map<EntityKey<?>, EntityEntry> persistenceContextMap);

  /**
   * Looks for updated fields in the given <code>entityEntry</code>.
   *
   * @param entityEntry entity entry
   *
   * @return fields that should be updated in a database
   */
  DirtyFieldMapping<?>[] getUpdatedFields(EntityEntry entityEntry);

  public Map<String, Object> getSnapshot(EntityMapping entityMapping, Object entity);

}