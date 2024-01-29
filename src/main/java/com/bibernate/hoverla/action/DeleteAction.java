package com.bibernate.hoverla.action;

import com.bibernate.hoverla.session.EntityPersister;

/**
 * Represents an action for deleting an entity.
 * <p/>
 * This class extends the {@code EntityAction} and is specifically used for deleting entities.
 */
public class DeleteAction extends EntityAction {

  /**
   * Constructs a new delete action for the specified entity and entity persister.
   *
   * @param entity          The entity instance to delete.
   * @param entityPersister The entity persister responsible for handling the entity.
   */
  protected DeleteAction(Object entity, EntityPersister entityPersister) {
    super(entity, entityPersister);
  }

  /**
   * Executes the delete action by invoking the delete method on the associated entity persister.
   */
  @Override
  public void execute() {
    entityPersister.delete(entity);
  }

  /**
   * Gets the priority of the delete action.
   *
   * @return The priority value, which is set to 30 for delete actions.
   */
  @Override
  public int priority() {
    return 30;
  }

  @Override
  boolean executeImmediately() {
    return false;
  }

}