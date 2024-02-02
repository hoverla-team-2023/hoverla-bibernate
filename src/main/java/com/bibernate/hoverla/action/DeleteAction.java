package com.bibernate.hoverla.action;

import com.bibernate.hoverla.session.EntityDaoService;

/**
 * Represents an action for deleting an entity.
 * <p/>
 * This class extends the {@code EntityAction} and is specifically used for deleting entities.
 */
public class DeleteAction extends EntityAction {

  /**
   * Constructs a new delete action for the specified entity and entity dao service.
   *
   * @param entity          The entity instance to delete.
   * @param entityDaoService The entity dao service responsible for handling the entity.
   */
  protected DeleteAction(Object entity, EntityDaoService entityDaoService) {
    super(entity, entityDaoService);
  }

  /**
   * Executes the delete action by invoking the delete method on the associated entity dao service.
   */
  @Override
  public void execute() {
    entityDaoService.delete(entity);
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