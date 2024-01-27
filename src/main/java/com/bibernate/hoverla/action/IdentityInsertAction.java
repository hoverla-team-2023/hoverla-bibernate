package com.bibernate.hoverla.action;

import com.bibernate.hoverla.session.EntityPersister;

/**
 * Represents an action for inserting an entity with identity (auto-generated) key values.
 * <p>
 * This class extends the {@code InsertAction} and is specifically used for inserting entities with
 * auto-generated identity key values.
 *
 * @see InsertAction
 */
public class IdentityInsertAction extends InsertAction {

  /**
   * Constructs a new identity insert action for the specified entity and entity persister.
   *
   * @param entity          The entity instance to insert.
   * @param entityPersister The entity persister responsible for handling the entity.
   */
  public IdentityInsertAction(Object entity, EntityPersister entityPersister) {
    super(entity, entityPersister);
  }

}
