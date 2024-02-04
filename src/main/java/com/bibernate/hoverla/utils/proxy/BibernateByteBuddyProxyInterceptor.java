package com.bibernate.hoverla.utils.proxy;

import java.lang.reflect.Method;

import org.apache.commons.lang3.NotImplementedException;

import com.bibernate.hoverla.exceptions.LazyLoadingException;
import com.bibernate.hoverla.session.Session;

import lombok.Getter;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperMethod;
import net.bytebuddy.implementation.bind.annotation.This;

/**
 * Represents an interceptor for BibernateByteBuddyProxy objects.
 * The interceptor is responsible for lazy loading and method interception.
 */
@Getter
public class BibernateByteBuddyProxyInterceptor {

  /**
   * The name of the interceptor field in the proxy class.
   */
  public static final String INTERCEPTOR_FIELD_NAME = "$$bibernate_interceptor";

  private Session session;

  private final Class<?> entityClass;

  private final Object entityId;

  private Object loadedEntity;

  /**
   * Constructs a new BibernateByteBuddyProxyInterceptor with the given session, entity class, and entity ID.
   *
   * @param session     The session object used for lazy loading.
   * @param entityClass The class of the entity being proxied.
   * @param entityId    The ID of the entity.
   */
  public BibernateByteBuddyProxyInterceptor(Session session, Class<?> entityClass, Object entityId) {
    this.session = session;
    this.entityClass = entityClass;
    this.entityId = entityId;
  }

  /**
   * Intercepts method calls on the proxy object.
   *
   * @param self        The proxy object.
   * @param method      The method being invoked.
   * @param args        The arguments passed to the method.
   * @param superMethod A callable that represents the original method call.
   *
   * @return The result of the method call.
   *
   * @throws Throwable If an error occurs during the method call.
   */
  @RuntimeType
  public Object intercept(@This Object self,
                          @Origin Method method,
                          @AllArguments Object[] args,
                          @SuperMethod Method superMethod) throws Throwable {
    if (isIdGetter(method)) {
      return entityId;
    }
    if (loadedEntity == null) {
      if (session == null) {
        throw new LazyLoadingException("Failed to load entity: session is null.");
      }
      throw new NotImplementedException("TODO use session find method");
    }
    return method.invoke(loadedEntity, args);
  }

  /**
   * Checks if the given method is a getter for the entity's ID.
   *
   * @param method The method to check.
   *
   * @return True if the method is a getter for the ID, false otherwise.
   */
  private boolean isIdGetter(Method method) {
    return method.getName().equals("getId"); // todo get it from metamodel
  }

  /**
   * Unlinks the session from this interceptor, preventing lazy loading if session is closed.
   */
  public void unlinkSession() {
    this.session = null;
  }

}
