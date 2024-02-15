package com.bibernate.hoverla.session.cache;
/** The EntityState enum represents the different states that an entity object can be in. */
public enum EntityState {
  /**
   * The entity object has been instantiated and is not associated with a persistence context yet.
   * It does not yet have an identity in the database and any changes made to it will not be
   * persisted to the database unless it is explicitly made persistent.
   */
  TRANSIENT,
  /**
   * The entity object is associated with a persistence context and has an identity in the database.
   * Any changes made to the entity will be tracked by the persistence context and will be
   * automatically persisted to the database when the transaction is committed.
   */
  MANAGED,
  /**
   * The entity object was previously managed by a persistence context, but is no longer associated
   * with one. Any changes made to the entity will not be tracked by the persistence context and
   * will not be persisted to the database when the transaction is committed. If the entity needs to
   * be persisted again, it needs to be re-attached to a persistence context
   */
  DETACHED,
  /**
   * The entity object has been marked for removal from the database. When the transaction is
   * committed, the entity will be removed from the database. Note that this is a separate state
   * from TRANSIENT, because even though the object is not associated with the persistence context
   * anymore, it is still managed by it and the changes will still be persisted to the database when
   * the transaction is committed
   */
  REMOVED
}
