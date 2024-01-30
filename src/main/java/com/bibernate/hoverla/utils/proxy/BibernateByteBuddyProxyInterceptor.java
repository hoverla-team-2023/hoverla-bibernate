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

  public static final String INTERCEPTOR_FIELD_NAME = "$$bibernate_interceptor";

  private Session session;

  private final Class<?> entityClass;

  private final Object entityId;

  private Object loadedEntity;

  public BibernateByteBuddyProxyInterceptor(Session session, Class<?> entityClass, Object entityId) {
    this.session = session;
    this.entityClass = entityClass;
    this.entityId = entityId;
  }

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

  private boolean isIdGetter(Method method) {
    return method.getName().equals("getId"); // todo get it from metamodel
  }

  public void unlinkSession() {
    this.session = null;
  }

}
