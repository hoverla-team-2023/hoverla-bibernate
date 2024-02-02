package com.bibernate.hoverla.action;

import com.bibernate.hoverla.session.EntityDaoService;

/**
 * Represents an action for updating an entity.
 *
 * This class extends the {@code EntityAction} and is specifically used for updating entities.
 */
public class UpdateAction extends EntityAction {

  /**
   * Constructs a new update action for the specified entity and entity dao service.
   *
   * @param entity          The entity instance to update.
   * @param entityDaoService The entity dao service responsible for handling the entity.
   */
  public UpdateAction(Object entity, EntityDaoService entityDaoService) {
    super(entity, entityDaoService);
  }

  /**
   * Executes the update action by invoking the update method on the associated entity dao service.
   */
  @Override
  public void execute() {
    entityDaoService.update(entity);
  }

  /**
   * Gets the priority of the update action.
   *
   * @return The priority value, which is set to 20 for update actions.
   */
  @Override
  public int priority() {
    return 20;
  }

  @Override
  boolean executeImmediately() {
    return false;
  }

}
