package com.bibernate.hoverla.session.cache;

import com.bibernate.hoverla.session.LockMode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A class representing an entry in the persistence context.
 * This class encapsulates the state and metadata of an entity in the context of a session.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class EntityEntry {

  /**
   * The entity instance associated with this entry.
   */
  private Object entity;

  /**
   * The lock mode for the entity.
   */
  private LockMode lockMode;

  /**
   * Indicates whether the entity is read-only.
   */
  private boolean isReadOnly;

  /**
   * A snapshot of the entity's field values at the time of loading or last update.
   */
  private Object[] snapshot;

  /**
   * The current state of the entity in the persistence context.
   */
  private EntityState entityState;

  /**
   * Indicates whether the entity is a proxy.
   */
  private boolean isProxy;

}
