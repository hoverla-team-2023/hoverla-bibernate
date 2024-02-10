package com.bibernate.hoverla.session;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.bibernate.hoverla.annotations.Column;
import com.bibernate.hoverla.annotations.Entity;
import com.bibernate.hoverla.annotations.Id;
import com.bibernate.hoverla.annotations.ManyToOne;
import com.bibernate.hoverla.jdbc.PostgresSqlTestExtension;
import com.bibernate.hoverla.jdbc.types.provider.JdbcTypeProviderImpl;
import com.bibernate.hoverla.metamodel.Metamodel;
import com.bibernate.hoverla.metamodel.scan.MetamodelScanner;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SessionRemoveITest {

  @RegisterExtension
  static PostgresSqlTestExtension DB = new PostgresSqlTestExtension("session-remove/init.sql", "session-remove/clear.sql");

  @Order(10)
  @Test
  void testRemove() {
    MetamodelScanner metamodelScanner = new MetamodelScanner(new JdbcTypeProviderImpl());
    Metamodel metamodel = metamodelScanner.scanEntities(TestEntity.class, TestComment.class);
    SessionFactory sessionFactory = new SessionFactoryImpl(DB.getDataSource(), metamodel);

    sessionFactory.inTransaction(session -> {
      TestEntity testEntity = session.find(TestEntity.class, 1L);
      Assertions.assertNotNull(testEntity);
      session.remove(testEntity);
    });

    TestEntity testEntity = sessionFactory.fromTransaction(session -> session.find(TestEntity.class, 1L));
    Assertions.assertNull(testEntity);

  }

  @Entity
  @NoArgsConstructor
  @ToString
  @Getter
  @Setter
  public static class TestEntity {

    @Id
    private Long id;
    @Column(name = "first_name")
    private String firstName;
    @Column(name = "last_name")
    private String lastName;
    private String email;

    @ManyToOne
    @Column(name = "comment_id")
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

  }

}