package com.bibernate.hoverla.session.cache;

import java.util.Map;

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
  private Map<String, Object> snapshot; // snapshot of the entity. Field name used as a map key. Field value used as a map value.
  private EntityState entityState;
  private boolean isProxy;

}
