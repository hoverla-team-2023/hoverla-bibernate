package com.bibernate.hoverla.session.cache;

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
  private boolean isReadOnly;
  private Object[] snapshot;
  private EntityState entityState;

}
