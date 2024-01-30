package com.bibernate.hoverla.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.bibernate.hoverla.exceptions.BibernateException;
import com.bibernate.hoverla.exceptions.LazyLoadingException;
import com.bibernate.hoverla.session.Session;
import com.bibernate.hoverla.utils.proxy.BibernateByteBuddyProxyInterceptor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class EntityProxyUtilsTest {

  @Mock
  private Session session;

  @Test
  public void whenCreateProxy_thenCheckTheProxyIsNotNull() {
    User proxy = EntityProxyUtils.createProxy(session, User.class, 1L);
    assertNotNull(proxy);
  }

  @Test
  public void whenIsProxy_thenAssert() {
    User proxy = EntityProxyUtils.createProxy(session, User.class, 1L);

    assertTrue(EntityProxyUtils.isProxy(proxy));
    assertFalse(EntityProxyUtils.isProxy(User.builder().id(5L).name("Test").comment("Comment").build()));
  }

  @Test
  public void whenCreateProxyForFinalClass_thenThrownException() {
    Assertions.assertThrowsExactly(BibernateException.class, () -> EntityProxyUtils.createProxy(session, Person.class, 1L));
  }

  @Test
  public void whenGetInterceptor_thenVerityInterceptor() {
    User proxy = EntityProxyUtils.createProxy(session, User.class, 155L);
    BibernateByteBuddyProxyInterceptor interceptor = EntityProxyUtils.getInterceptor(proxy);

    assertNotNull(interceptor);
    assertEquals(155L, interceptor.getEntityId());
    assertEquals(User.class, interceptor.getEntityClass());
    assertNull(interceptor.getLoadedEntity());
    assertNotNull(interceptor.getSession());
  }

  @Test
  public void test() {
    User proxy = EntityProxyUtils.createProxy(session, User.class, 155L);
    BibernateByteBuddyProxyInterceptor interceptor = EntityProxyUtils.getInterceptor(proxy);

    assertNotNull(interceptor);
    interceptor.unlinkSession();
    Assertions.assertThrowsExactly(LazyLoadingException.class, proxy::getComment);
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class User {

    private Long id;
    private String name;
    private String comment;

  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static final class Person {

    private Long id;
    private String name;
    private String comment;

  }

}

