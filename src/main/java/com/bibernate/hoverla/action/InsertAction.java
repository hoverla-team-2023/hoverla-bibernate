package com.bibernate.hoverla.action;

import com.bibernate.hoverla.session.EntityDaoService;

/**
 * Represents an action for inserting an entity.
 * <p>
 * This class extends the {@code EntityAction} and is specifically used for inserting entities.
 */
public class InsertAction extends EntityAction {

  /**
   * Constructs a new insert action for the specified entity and entity persister.
   *
   * @param entity          The entity instance to insert.
   * @param entityDaoService The entity persister responsible for handling the entity.
   */
  public InsertAction(Object entity, EntityDaoService entityDaoService) {
    super(entity, entityDaoService);
  }

  /**
   * Executes the insert action by invoking the insert method on the associated entity persister.
   */
  @Override
  public void execute() {
    entityDaoService.insert(entity);
  }

  /**
   * Gets the priority of the insert action.
   *
   * @return The priority value, which is set to 10 for insert actions.
   */
  @Override
  public int priority() {
    return 10;
  }

  @Override
  boolean executeImmediately() {
    return false;
  }

}
