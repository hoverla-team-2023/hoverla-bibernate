package com.bibernate.hoverla.utils.proxy;

import java.lang.reflect.Method;

import org.apache.commons.lang3.StringUtils;

import com.bibernate.hoverla.exceptions.LazyLoadingException;
import com.bibernate.hoverla.session.SessionImplementor;
import com.bibernate.hoverla.session.cache.EntityKey;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;

/**
 * Represents an interceptor for BibernateByteBuddyProxy objects.
 * The interceptor is responsible for lazy loading and method interception.
 */
@Slf4j
@Getter
public class BibernateByteBuddyProxyInterceptor<T> {

  /**
   * The name of the interceptor field in the proxy class.
   */
  public static final String INTERCEPTOR_FIELD_NAME = "$$bibernate_interceptor";

  private SessionImplementor session;

  private final Class<T> entityClass;

  private final Object entityId;

  private Object loadedEntity;

  private final String getIdMethodName;

  /**
   * Constructs a new BibernateByteBuddyProxyInterceptor with the given session, entity class, and entity ID.
   *
   * @param session     The session object used for lazy loading.
   * @param entityClass The class of the entity being proxied.
   * @param entityId    The ID of the entity.
   */
  public BibernateByteBuddyProxyInterceptor(SessionImplementor session, Class<T> entityClass, Object entityId) {
    this.session = session;
    this.entityClass = entityClass;
    this.entityId = entityId;
    this.getIdMethodName = getGetIdMethodName(session, entityClass);
  }

  private String getGetIdMethodName(SessionImplementor session, Class<T> entityClass) {
    return "get" + StringUtils.capitalize(session.getEntityMapping(entityClass).getPrimaryKeyMapping().getFieldName());
  }

  /**
   * Intercepts method calls on the proxy object.
   *
   * @param method The method being invoked.
   * @param args   The arguments passed to the method.
   *
   * @return The result of the method call.
   *
   * @throws Throwable If an error occurs during the method call.
   */
  @RuntimeType
  public Object intercept(@Origin Method method,
                          @AllArguments Object[] args) throws Throwable {
    if (isIdGetter(method)) {
      return entityId;
    }
    loadProxy();
    return method.invoke(loadedEntity, args);
  }

  /**
   * Sets the value of a property on the loaded entity using a setter method.
   *
   * @param object the value to set on the property.
   * @param setter the setter method to invoke.
   *
   * @throws Throwable if an error occurs during the invocation of the setter method.
   */
  public void set(Object object, @Origin Method setter) throws Throwable {
    loadProxy();
    setter.invoke(loadedEntity, object);
  }

  /**
   * Lazily loads the entity if not already loaded.
   */
  public void loadProxy() {
    if (this.loadedEntity == null) {
      log.debug("initializing lazy loading");
      if (session == null) {
        throw new LazyLoadingException("Failed to load entity: session is null.");
      }
      EntityKey<T> entityKey = new EntityKey<>(entityClass, entityId);
      Object loaded = session.getEntityDaoService().load(entityKey);
      session.getPersistenceContext().manageEntity(entityKey, () -> loaded, entry -> {});
      if (loaded == null) {
        throw new LazyLoadingException("Failed to load entity: entity was not found.");
      }
      this.loadedEntity = loaded;
    }
  }

  /**
   * Checks if the given method is a getter for the entity's ID.
   *
   * @param method The method to check.
   *
   * @return True if the method is a getter for the ID, false otherwise.
   */
  private boolean isIdGetter(Method method) {
    return method.getName().equals(getIdMethodName); // todo get it from metamodel
  }

  /**
   * Unlinks the session from this interceptor, preventing lazy loading if session is closed.
   */
  public void unlinkSession() {
    this.session = null;
  }

  /**
   * Initializes the loaded entity if not already initialized.
   *
   * @param loadedEntity the loaded entity object.
   */
  public void initializyIfEmpty(Object loadedEntity) {
    if (this.loadedEntity == null) {
      this.loadedEntity = loadedEntity;
      //todo save snapshot
    }
  }

}
