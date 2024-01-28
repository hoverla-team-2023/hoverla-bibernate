package com.bibernate.hoverla.session.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import com.bibernate.hoverla.session.cache.EntityKey;

import lombok.extern.slf4j.Slf4j;
/**
 * Implements all methods for Hoverla Session
 */

@Slf4j
public class HoverlaSessionService {

  private final Connection connection;

  public HoverlaSessionService(Connection connection) {
    this.connection = connection;
  }

  public <T> T findOne(EntityKey<T> key) {

    return null;
  }

  public <T> List<T> findAllByType(Class<T> type, Map<EntityKey<?>, Object> entitiesCacheMap,
                                   Map<EntityKey<?>, Map<String, Object>> entitiesSnapshotMap) {

    return null;
  }

  public <T> List<T> findAllBy(

    Map<EntityKey<?>, Object> entitiesCacheMap, Map<EntityKey<?>, Map<String, Object>> entitiesSnapshotMap) {
    return null;
  }

  private <T> List<T> executeCollectionSelectQuery(Class<T> type,
                                                   PreparedStatement statement,
                                                   Map<EntityKey<?>, Object> entitiesCacheMap,
                                                   Map<EntityKey<?>, Map<String, Object>> entitiesSnapshotMap) {

    List<T> retrievedEntities = new ArrayList<T>();

    return retrievedEntities;
  }

}