package com.bibernate.hoverla.utils;

import java.lang.reflect.Field;

import com.bibernate.hoverla.exceptions.BibernateException;
import com.bibernate.hoverla.session.Session;
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
   * @param session    The session object to use for lazy loading.
   * @param entityType The entity type of the proxy object.
   * @param entityId   The ID of the entity.
   * @param <T>        The generic type of the entity.
   *
   * @return The proxy object of the entity type.
   *
   * @throws BibernateException if the proxy creation fails.
   */

  public static <T> T createProxy(Session session, Class<T> entityType, Object entityId) {
    BibernateByteBuddyProxyInterceptor interceptor = new BibernateByteBuddyProxyInterceptor(session, entityType, entityId);

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

  /**
   * Retrieves the interceptor object associated with the given proxy object.
   *
   * @param proxy The proxy object.
   *
   * @return The BibernateByteBuddyProxyInterceptor object associated with the proxy object, or null if not found.
   */

  public static BibernateByteBuddyProxyInterceptor getProxyInterceptor(Object proxy) {
    try {
      Field field = proxy.getClass().getDeclaredField(INTERCEPTOR_FIELD_NAME);
      field.setAccessible(true);
      return (BibernateByteBuddyProxyInterceptor) field.get(proxy);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      return null;
    }
  }

}
