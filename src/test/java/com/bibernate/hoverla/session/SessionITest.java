package com.bibernate.hoverla.session;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.bibernate.hoverla.annotations.Column;
import com.bibernate.hoverla.annotations.Entity;
import com.bibernate.hoverla.annotations.Id;
import com.bibernate.hoverla.annotations.IdentityGeneratedValue;
import com.bibernate.hoverla.annotations.ManyToOne;
import com.bibernate.hoverla.annotations.OneToMany;
import com.bibernate.hoverla.annotations.SequenceGeneratedValue;
import com.bibernate.hoverla.annotations.Table;
import com.bibernate.hoverla.exceptions.LazyLoadingException;
import com.bibernate.hoverla.jdbc.PostgresSqlTestExtension;
import com.bibernate.hoverla.jdbc.types.provider.JdbcTypeProviderImpl;
import com.bibernate.hoverla.metamodel.Metamodel;
import com.bibernate.hoverla.metamodel.scan.MetamodelScanner;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SessionITest {

  @RegisterExtension
  static PostgresSqlTestExtension DB = new PostgresSqlTestExtension("session-test/init.sql", "session-test/clear.sql");

  private SessionFactory sessionFactory;

  @Order(10)
  @Test
  void testLazyLoadingOfCollection() {
    MetamodelScanner metamodelScanner = new MetamodelScanner(new JdbcTypeProviderImpl());
    Metamodel metamodel = metamodelScanner.scanEntities(TestEntity.class, TestComment.class);
    this.sessionFactory = new SessionFactoryImpl(DB.getDataSource(), metamodel);

    sessionFactory.inTransaction(session -> {
      TestComment testEntity = session.find(TestComment.class, 1L);
      assertEquals(2, testEntity.testEntities.size());
    });

  }

  @Order(6)
  @Test
  void testPessimisticForShareLock() {
    MetamodelScanner metamodelScanner = new MetamodelScanner(new JdbcTypeProviderImpl());
    Metamodel metamodel = metamodelScanner.scanEntities(TestEntity.class, TestComment.class);
    this.sessionFactory = new SessionFactoryImpl(DB.getDataSource(), metamodel);

    sessionFactory.inTransaction(session -> {
      session.find(TestComment.class, 1L, LockMode.FOR_SHARE);
    });

  }

  @Order(8)
  @Test
  void testPessimisticForUpdateLock() {
    MetamodelScanner metamodelScanner = new MetamodelScanner(new JdbcTypeProviderImpl());
    Metamodel metamodel = metamodelScanner.scanEntities(TestEntity.class, TestComment.class);
    this.sessionFactory = new SessionFactoryImpl(DB.getDataSource(), metamodel);

    sessionFactory.inTransaction(session -> {
      session.find(TestComment.class, 1L, LockMode.FOR_UPDATE);
    });

  }

  @Order(15)
  @Test
  void whenMergeDetachedEntity_thenEntityMerged() {
    String commentText = "newComment";
    MetamodelScanner metamodelScanner = new MetamodelScanner(new JdbcTypeProviderImpl());
    Metamodel metamodel = metamodelScanner.scanEntities(TestEntity.class, TestComment.class);
    this.sessionFactory = new SessionFactoryImpl(DB.getDataSource(), metamodel);

    TestComment testComment = sessionFactory.fromTransaction(session -> session.find(TestComment.class, 1L));
    testComment.setComment(commentText);

    sessionFactory.inTransaction(session -> {
      session.merge(testComment);
    });

    TestComment newComment = sessionFactory.fromTransaction(session -> session.find(TestComment.class, 1L));
    assertEquals(commentText, newComment.getComment());
  }

  @Order(20)
  @Test
  void whenTryingToInitializeCollectionAftersSessionIsClosed_thenLazyLoadingException() {
    MetamodelScanner metamodelScanner = new MetamodelScanner(new JdbcTypeProviderImpl());
    Metamodel metamodel = metamodelScanner.scanEntities(TestEntity.class, TestComment.class);
    this.sessionFactory = new SessionFactoryImpl(DB.getDataSource(), metamodel);

    TestComment testComment = sessionFactory.fromSession(session -> session.find(TestComment.class, 1L));

    assertThrows(LazyLoadingException.class, () -> testComment.getTestEntities().size());

  }

  @Order(30)
  @Test
  void whenTryingToInitializeEntityAftersSessionIsClosed_thenLazyLoadingException() {
    MetamodelScanner metamodelScanner = new MetamodelScanner(new JdbcTypeProviderImpl());
    Metamodel metamodel = metamodelScanner.scanEntities(TestEntity.class, TestComment.class);
    this.sessionFactory = new SessionFactoryImpl(DB.getDataSource(), metamodel);

    TestComment testComment = sessionFactory.fromSession(session -> session.getReference(TestComment.class, 1L));

    assertThrows(LazyLoadingException.class, () -> testComment.getComment());

  }

  @Order(35)
  @Test
  void whenRemoveAndDetach_verifyEntityIsNotRemoved() {
    MetamodelScanner metamodelScanner = new MetamodelScanner(new JdbcTypeProviderImpl());
    Metamodel metamodel = metamodelScanner.scanEntities(TestEntityIdentity.class, TestEntity.class, TestComment.class);
    this.sessionFactory = new SessionFactoryImpl(DB.getDataSource(), metamodel);

    sessionFactory.inTransaction(session -> {
      TestEntity testEntity = session.find(TestEntity.class, 1L);
      Assertions.assertNotNull(testEntity);
      session.remove(testEntity);
      session.detach(testEntity);
    });

    TestEntityIdentity testEntity = sessionFactory.fromTransaction(session -> session.find(TestEntityIdentity.class, 1L));
    Assertions.assertNotNull(testEntity);
  }

  @Order(40)
  @Test
  void whenRemove_verifyEntityIsRemoved() {
    MetamodelScanner metamodelScanner = new MetamodelScanner(new JdbcTypeProviderImpl());
    Metamodel metamodel = metamodelScanner.scanEntities(TestEntityIdentity.class, TestEntity.class, TestComment.class);
    this.sessionFactory = new SessionFactoryImpl(DB.getDataSource(), metamodel);

    sessionFactory.inTransaction(session -> {
      TestEntity testEntity = session.find(TestEntity.class, 1L);
      Assertions.assertNotNull(testEntity);
      session.remove(testEntity);
    });

    TestEntityIdentity testEntity = sessionFactory.fromTransaction(session -> session.find(TestEntityIdentity.class, 1L));
    Assertions.assertNull(testEntity);
  }

  @Order(50)
  @Test
  void whenPersistWithSequenceStrategy_verifyTheIdIsPresent() {
    MetamodelScanner metamodelScanner = new MetamodelScanner(new JdbcTypeProviderImpl());
    Metamodel metamodel = metamodelScanner.scanEntities(TestEntityIdentity.class, TestEntity.class, TestComment.class);
    this.sessionFactory = new SessionFactoryImpl(DB.getDataSource(), metamodel);

    TestEntity persist = TestEntity.builder()
      .firstName("testPersist")
      .lastName("testPersist")
      .email("email")
      .build();

    this.sessionFactory.inTransaction(session -> {
      session.persist(persist);
    });

    Assertions.assertNotNull(persist.getId());
  }

  @Order(60)
  @Test
  void whenPersistWithIdentityStrategy_verifyTheIdIsPresent() {
    MetamodelScanner metamodelScanner = new MetamodelScanner(new JdbcTypeProviderImpl());
    Metamodel metamodel = metamodelScanner.scanEntities(TestEntityIdentity.class, TestEntity.class, TestComment.class);
    this.sessionFactory = new SessionFactoryImpl(DB.getDataSource(), metamodel);

    TestEntityIdentity persist = TestEntityIdentity.builder()
      .firstName("testPersist")
      .lastName("testPersist")
      .email("email")
      .build();

    this.sessionFactory.inTransaction(session -> {
      session.persist(persist);
    });

    Assertions.assertNotNull(persist.getId());
  }

  @Data
  @Entity
  @Builder
  @ToString
  @NoArgsConstructor
  @AllArgsConstructor
  public static class TestEntity {

    @Id
    @SequenceGeneratedValue(sequenceName = "test_entity_id_seq")
    private Long id;
    @Column(name = "first_name")
    private String firstName;
    @Column(name = "last_name")
    private String lastName;
    private String email;

    @ManyToOne
    @Column(name = "comment_id", updatable = false)
    TestComment comment;

  }

  @Data
  @Entity
  @Builder
  @ToString
  @NoArgsConstructor
  @AllArgsConstructor
  @Table("test_entity")
  public static class TestEntityIdentity {

    @Id
    @IdentityGeneratedValue
    private Long id;
    @Column(name = "first_name")
    private String firstName;
    @Column(name = "last_name")
    private String lastName;
    private String email;

    @ManyToOne
    @Column(name = "comment_id", updatable = false)
    TestComment comment;

  }

  @Entity
  @NoArgsConstructor
  @ToString
  @Getter
  @Setter
  public static class TestComment {

    @Id
    private Long id;

    @Column(name = "comment")
    private String comment;

    @OneToMany(mappedBy = "comment")
    List<TestEntity> testEntities;

  }

}
