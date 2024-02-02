package com.bibernate.hoverla.action;

import com.bibernate.hoverla.session.EntityDaoService;

/**
 * Base class for actions relating to entities, such as insert, update, or delete operations.
 * Subclasses should provide concrete implementations for action execution and priority determination.
 */
public abstract class EntityAction {

  /**
   * The entity instance associated with the action.
   */
  protected final Object entity;

  /**
   * The entity persister responsible for handling the entity.
   */
  protected final EntityDaoService entityDaoService;

  /**
   * Constructs a new entity action with the specified entity and entity persister.
   *
   * @param entity          The entity instance.
   * @param entityDaoService The entity persister.
   */
  public EntityAction(Object entity, EntityDaoService entityDaoService) {
    this.entity = entity;
    this.entityDaoService = entityDaoService;
  }

  /**
   * Executes the entity action.
   */
  abstract void execute();

  /**
   * Gets the priority of the entity action.
   *
   * @return The priority value.
   */
  abstract int priority();

  /**
   * Determines whether the entity action should be executed immediately or deferred.
   *
   * @return `true` if the action should be executed immediately, `false` if it can be deferred.
   */
  abstract boolean executeImmediately();

}
