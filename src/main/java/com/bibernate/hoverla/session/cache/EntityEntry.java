package com.bibernate.hoverla.session.cache;

import com.bibernate.hoverla.session.LockMode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class EntityEntry {

  private Object entity;
  private LockMode lockMode;
  private boolean isReadOnly;
  private Object[] snapshot;
  private EntityState entityState;
  private boolean isProxy;

}
