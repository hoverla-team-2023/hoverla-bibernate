package com.bibernate.hoverla.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.bibernate.hoverla.exceptions.BibernateException;
import com.bibernate.hoverla.exceptions.LazyLoadingException;
import com.bibernate.hoverla.metamodel.EntityMapping;
import com.bibernate.hoverla.metamodel.FieldMapping;
import com.bibernate.hoverla.session.SessionImplementor;
import com.bibernate.hoverla.session.cache.EntityKey;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class EntityProxyUtilsTest {

  @Mock
  private SessionImplementor session;

  @Test
  public void whenCreateProxy_thenCheckTheProxyIsNotNull() {
    mockEntityMapping();

    User proxy = EntityProxyUtils.createProxy(session, new EntityKey<>(User.class, 1L));
    assertNotNull(proxy);
  }

  @Test
  public void whenIsProxy_thenAssert() {
    mockEntityMapping();

    User proxy = EntityProxyUtils.createProxy(session, new EntityKey<>(User.class, 1L));

    assertTrue(EntityProxyUtils.isProxy(proxy));
    assertFalse(EntityProxyUtils.isProxy(User.builder().id(5L).name("Test").comment("Comment").build()));
  }

  @Test
  public void whenCreateProxyForFinalClass_thenThrownException() {
    EntityMapping entityMapping = mock(EntityMapping.class);
    doReturn(entityMapping).when(session).getEntityMapping(Person.class);
    doReturn(FieldMapping.builder().fieldName("id").build()).when(entityMapping).getPrimaryKeyMapping();

    Assertions.assertThrowsExactly(BibernateException.class, () -> EntityProxyUtils.createProxy(session, new EntityKey<>(Person.class, 1L)));
  }

  @Test
  public void whenGetInterceptor_thenVerityInterceptor() {
    mockEntityMapping();

    User proxy = EntityProxyUtils.createProxy(session, new EntityKey<>(User.class, 155L));
    var interceptor = EntityProxyUtils.getProxyInterceptor(proxy);

    assertNotNull(interceptor);
    assertEquals(155L, interceptor.getEntityId());
    assertEquals(User.class, interceptor.getEntityClass());
    assertNull(interceptor.getLoadedEntity());
    assertNotNull(interceptor.getSession());
  }

  @Test
  public void whenUnlinkSession_thenLazyLoadingExceptionThrown() {

    mockEntityMapping();

    User proxy = EntityProxyUtils.createProxy(session, new EntityKey<>(User.class, 155L));
    var interceptor = EntityProxyUtils.getProxyInterceptor(proxy);

    assertNotNull(interceptor);
    interceptor.unlinkSession();
    Assertions.assertThrowsExactly(LazyLoadingException.class, proxy::getComment);
  }

  @Test
  public void whenUpdateProxy_verifySettersIntercepted() {
    mockEntityMapping();
    String comment = "Comment";
    String newComment = "Update";

    User proxy = EntityProxyUtils.createProxy(session, new EntityKey<>(User.class, 1L));
    User user = User.builder().id(5L).name("Test").comment(comment).build();

    EntityProxyUtils.initializeProxy(proxy, user);
    assertEquals(comment, proxy.getComment());

    proxy.setComment(newComment);
    assertEquals(newComment, proxy.getComment());
  }

  private void mockEntityMapping() {
    EntityMapping entityMapping = mock(EntityMapping.class);
    doReturn(entityMapping).when(session).getEntityMapping(User.class);
    doReturn(FieldMapping.builder().fieldName("id").build()).when(entityMapping).getPrimaryKeyMapping();
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

