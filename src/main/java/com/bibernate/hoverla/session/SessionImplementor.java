package com.bibernate.hoverla.session;

import java.sql.Connection;

import com.bibernate.hoverla.action.ActionQueue;
import com.bibernate.hoverla.exceptions.BibernateException;
import com.bibernate.hoverla.jdbc.JdbcExecutor;
import com.bibernate.hoverla.metamodel.EntityMapping;
import com.bibernate.hoverla.metamodel.FieldMapping;
import com.bibernate.hoverla.session.cache.EntityKey;
import com.bibernate.hoverla.session.cache.PersistenceContext;
import com.bibernate.hoverla.utils.EntityUtils;
import com.bibernate.hoverla.utils.proxy.BibernateByteBuddyProxyInterceptor;

import static com.bibernate.hoverla.utils.EntityProxyUtils.getProxyInterceptor;

/**
 * An internal contract that extends the Session interface with additional methods for internal framework usage.
 *
 * <p>This interface extends the Session interface to provide additional methods for accessing the session's
 * associated SessionFactory, PersistenceContext, ActionQueue, and JdbcExecutor. It is intended for internal
 * use within the framework and should not be used directly by external developers.</p>
 */
public interface SessionImplementor extends Session {

  /**
   * Retrieves the SessionFactoryImplementor associated with this session.
   *
   * @return The SessionFactoryImplementor used by this session.
   */
  SessionFactoryImplementor getSessionFactory();

  /**
   * Retrieves the first-level cache (PersistenceContext) used by this session.
   *
   * @return The first-level cache (PersistenceContext) associated with this session.
   */
  PersistenceContext getPersistenceContext();

  /**
   * Retrieves the ActionQueue associated with this session.
   *
   * @return The ActionQueue used by this session for managing EntityActions.
   */
  ActionQueue getActionQueue();

  /**
   * Retrieves the JdbcExecutor associated with this session.
   *
   * @return The JdbcExecutor used by this session for executing JDBC queries.
   */
  JdbcExecutor getJdbcExecutor();

  /**
   * Retrieves the EntityDaoService associated with this session.
   *
   * <p>The EntityDaoService is responsible for managing the persistence and operations of entity objects within the session.
   * It provides essential functionality for creating, updating, and removing entities in the data store.</p>
   *
   * @return The EntityDaoService used by this session for managing entity objects.
   */
  EntityDaoService getEntityDaoService();

  EntityRowMapper getEntityRowMapper();

  default <T> EntityMapping getEntityMapping(Class<T> entityClass) {
    EntityMapping entityMapping = getSessionFactory()
      .getMetamodel()
      .getEntityMappingMap()
      .get(entityClass);

    if (entityMapping == null) {
      throw new BibernateException("""
                                     The specified class %s is not registered as an entity.
                                     Ensure that the class has been added in configuration and marked as an entity.
                                     """
                                     .formatted(entityClass));
    }

    return entityMapping;
  }

  default  <T> EntityDetails getEntityDetails(T entity) {
    BibernateByteBuddyProxyInterceptor<T> proxyInterceptor = getProxyInterceptor(entity);
    boolean isProxy = proxyInterceptor != null;
    Class<?> entityClass = isProxy ? proxyInterceptor.getEntityClass() : entity.getClass();
    EntityMapping entityMapping = getEntityMapping(entityClass);
    FieldMapping<?> primaryKeyMapping = entityMapping.getPrimaryKeyMapping();
    EntityKey<T> entityKey = isProxy ? new EntityKey<>(proxyInterceptor.getEntityClass(), proxyInterceptor.getEntityId())
                                     : EntityUtils.getEntityKey((Class<T>) entity.getClass(), entity, primaryKeyMapping.getFieldName());

    return new EntityDetails(entityMapping, entityKey, isProxy);
  }
  //todo implement clear session caches
  void invalidateCaches();

  Connection getConnection();

}