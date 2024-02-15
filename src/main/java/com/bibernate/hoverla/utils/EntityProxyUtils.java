package com.bibernate.hoverla.utils;

import java.lang.reflect.Field;

import com.bibernate.hoverla.exceptions.BibernateException;
import com.bibernate.hoverla.session.SessionImplementor;
import com.bibernate.hoverla.session.cache.EntityKey;
import com.bibernate.hoverla.utils.proxy.BibernateByteBuddyProxyInterceptor;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.matcher.ElementMatchers;

import static com.bibernate.hoverla.utils.proxy.BibernateByteBuddyProxyInterceptor.INTERCEPTOR_FIELD_NAME;

/**
 * Utility class for creating and working with entity proxies.
 */
public class EntityProxyUtils {

  /**
   * Creates a proxy object of the given entity type with the provided session and entity ID.
   *
   * @param session   The session object to use for lazy loading.
   * @param entityKey The entity key of request proxy object.
   * @param <T>       The generic type of the entity.
   *
   * @return The proxy object of the entity type.
   *
   * @throws BibernateException if the proxy creation fails.
   */
  public static <T> T createProxy(SessionImplementor session, EntityKey<T> entityKey) {
    Class<T> entityType = entityKey.entityType();
    Object entityId = entityKey.id();
    var interceptor = new BibernateByteBuddyProxyInterceptor<>(session, entityType, entityId);

    try (DynamicType.Unloaded<T> dynamicType = new ByteBuddy()
      .subclass(entityType)
      .defineField(INTERCEPTOR_FIELD_NAME, BibernateByteBuddyProxyInterceptor.class, Opcodes.ACC_PRIVATE)
      .method(ElementMatchers.any())
      .intercept(MethodDelegation.to(interceptor))
      .make()) {
      T entity = dynamicType
        .load(entityType.getClassLoader())
        .getLoaded()
        .getDeclaredConstructor()
        .newInstance();

      setInterceptorField(entity, interceptor);

      return entity;
    } catch (Exception e) {
      throw new BibernateException("Failed to create proxy for %s, entityId: %s".formatted(entityType, entityId));
    }

  }
  /**
   * This static method sets the interceptor field of an entity to the provided interceptor.
   * The interceptor field is assumed to be named according to the constant INTERCEPTOR_FIELD_NAME.
   * The method uses reflection to access and modify the field.
   *
   * @param <T> The type of the entity.
   * @param entity The entity instance whose interceptor field will be set.
   * @param interceptor The interceptor to be set in the entity's interceptor field.
   * @throws NoSuchFieldException If the entity class does not have a field with the name INTERCEPTOR_FIELD_NAME.
   * @throws IllegalAccessException If the field cannot be accessed or modified due to security restrictions.
   */
  private static <T> void setInterceptorField(T entity, BibernateByteBuddyProxyInterceptor interceptor) throws NoSuchFieldException, IllegalAccessException {
    Field field = entity.getClass().getDeclaredField(INTERCEPTOR_FIELD_NAME);
    field.setAccessible(true);
    field.set(entity, interceptor);
  }

  /**
   * Determines whether an object is a proxy.
   *
   * @param object The object to check.
   *
   * @return true if the object is a proxy, false otherwise.
   */
  public static boolean isProxy(Object object) {
    return getProxyInterceptor(object) != null;
  }

  public static boolean isUnitializedProxy(Object object) {
    BibernateByteBuddyProxyInterceptor<Object> proxyInterceptor = getProxyInterceptor(object);
    return proxyInterceptor != null && proxyInterceptor.getLoadedEntity() == null;
  }

  /**
   * Retrieves the interceptor object associated with the given proxy object.
   *
   * @param proxy The proxy object.
   *
   * @return The BibernateByteBuddyProxyInterceptor object associated with the proxy object, or null if not found.
   */

  public static <T> BibernateByteBuddyProxyInterceptor<T> getProxyInterceptor(Object proxy) {
    try {
      Field field = proxy.getClass().getDeclaredField(INTERCEPTOR_FIELD_NAME);
      field.setAccessible(true);
      return (BibernateByteBuddyProxyInterceptor<T>) field.get(proxy);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      return null;
    }
  }

  public static <T> void initializeProxy(T proxy, T entity) {
    var proxyInterceptor = EntityProxyUtils.getProxyInterceptor(proxy);
    if (proxyInterceptor == null) {
      return;
    }

    proxyInterceptor.initializyIfEmpty(entity);
  }

  public static <T> T unProxy(T proxy) {
    var proxyInterceptor = EntityProxyUtils.getProxyInterceptor(proxy);
    if (proxyInterceptor == null) {
      return proxy;
    }

    return (T) proxyInterceptor.getLoadedEntity();
  }

  public static <T> T unProxyAndInitialize(T proxy) {
    var proxyInterceptor = EntityProxyUtils.getProxyInterceptor(proxy);
    if (proxyInterceptor == null) {
      return proxy;
    }

    proxyInterceptor.loadProxy();

    return (T) proxyInterceptor.getLoadedEntity();
  }

}
