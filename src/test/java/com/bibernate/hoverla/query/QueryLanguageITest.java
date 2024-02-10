package com.bibernate.hoverla.query;

import java.util.List;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.bibernate.hoverla.annotations.Column;
import com.bibernate.hoverla.annotations.Entity;
import com.bibernate.hoverla.annotations.Id;
import com.bibernate.hoverla.exceptions.BibernateBqlMissingParameterException;
import com.bibernate.hoverla.jdbc.PostgresSqlTestExtension;
import com.bibernate.hoverla.jdbc.types.provider.JdbcTypeProviderImpl;
import com.bibernate.hoverla.metamodel.Metamodel;
import com.bibernate.hoverla.metamodel.scan.MetamodelScanner;
import com.bibernate.hoverla.session.Session;
import com.bibernate.hoverla.session.SessionFactoryImpl;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class QueryLanguageITest {

  @RegisterExtension
  static PostgresSqlTestExtension DB = new PostgresSqlTestExtension("query/init.sql", "query/clear.sql");

  @Test
  void testQueryWithNamedParameters() {
    MetamodelScanner metamodelScanner = new MetamodelScanner(new JdbcTypeProviderImpl());
    Metamodel metamodel = metamodelScanner.scanEntities(TestEntity.class);
    SessionFactoryImpl sessionFactory = new SessionFactoryImpl(DB.getDataSource(), metamodel);

    try (Session session = sessionFactory.openSession()) {
      String firstNameParam = "FirsName1";
      String emailParam = "test2@email.com";
      List<TestEntity> result = session.createQuery(
          "WHERE firstName = :firstNameParam OR email = :emailParam",
          TestEntity.class)
        .setParameter("firstNameParam", firstNameParam)
        .setParameter("emailParam", emailParam)
        .getResult();

      assertNotNull(result);
      assertTrue(result.stream().anyMatch(entity -> emailParam.equals(entity.getEmail())));
      assertTrue(result.stream().anyMatch(entity -> firstNameParam.equals(entity.getFirstName())));
    }
  }

  @Test
  void testQueryWithIdsAsInParameter() {
    MetamodelScanner metamodelScanner = new MetamodelScanner(new JdbcTypeProviderImpl());
    Metamodel metamodel = metamodelScanner.scanEntities(TestEntity.class);
    SessionFactoryImpl sessionFactory = new SessionFactoryImpl(DB.getDataSource(), metamodel);

    try (Session session = sessionFactory.openSession()) {
      List<TestEntity> result = session.createQuery(
          "WHERE id IN :ids",
          TestEntity.class)
        .setParameter("ids", List.of(1))
        .getResult();

      assertNotNull(result);
      assertEquals(1, result.size());
    }
  }

  @Test
  void testQueryWithEmptyIdsCollectionAsInParameter() {
    MetamodelScanner metamodelScanner = new MetamodelScanner(new JdbcTypeProviderImpl());
    Metamodel metamodel = metamodelScanner.scanEntities(TestEntity.class);
    SessionFactoryImpl sessionFactory = new SessionFactoryImpl(DB.getDataSource(), metamodel);

    try (Session session = sessionFactory.openSession()) {
      List<TestEntity> result = session.createQuery(
          "WHERE id IN :ids",
          TestEntity.class)
        .setParameter("ids", List.of())
        .getResult();

      assertNotNull(result);
      assertEquals(2, result.size());
    }
  }

  @Test
  void testQueryWithNullIdsCollectionAsInParameter() {
    MetamodelScanner metamodelScanner = new MetamodelScanner(new JdbcTypeProviderImpl());
    Metamodel metamodel = metamodelScanner.scanEntities(TestEntity.class);
    SessionFactoryImpl sessionFactory = new SessionFactoryImpl(DB.getDataSource(), metamodel);

    try (Session session = sessionFactory.openSession()) {
      List<TestEntity> result = session.createQuery(
          "WHERE id IN :ids",
          TestEntity.class)
        .setParameter("ids", null)
        .getResult();

      assertNotNull(result);
      assertEquals(2, result.size());
    }
  }

  @Test
  void testQueryWithIdLessThanParameterReturnsCorrectSize() {
    MetamodelScanner metamodelScanner = new MetamodelScanner(new JdbcTypeProviderImpl());
    Metamodel metamodel = metamodelScanner.scanEntities(TestEntity.class);
    SessionFactoryImpl sessionFactory = new SessionFactoryImpl(DB.getDataSource(), metamodel);

    try (Session session = sessionFactory.openSession()) {
      List<TestEntity> result = session.createQuery(
          "WHERE id < :ids",
          TestEntity.class)
        .setParameter("ids", 2L)
        .getResult();

      assertNotNull(result);
      assertEquals(1, result.size());
    }
  }

  @Test
  void testBibernateBqlMissingParameterExceptionThrown() {
    MetamodelScanner metamodelScanner = new MetamodelScanner(new JdbcTypeProviderImpl());
    Metamodel metamodel = metamodelScanner.scanEntities(TestEntity.class);
    SessionFactoryImpl sessionFactory = new SessionFactoryImpl(DB.getDataSource(), metamodel);

    try (Session session = sessionFactory.openSession()) {
      assertThrows(BibernateBqlMissingParameterException.class, () -> session.createQuery(
          "WHERE id IN :ids",
          TestEntity.class)
        .getResult());
    }
  }

  @Test
  void testTheReferenceIsTheSame() {
    MetamodelScanner metamodelScanner = new MetamodelScanner(new JdbcTypeProviderImpl());
    Metamodel metamodel = metamodelScanner.scanEntities(TestEntity.class);
    SessionFactoryImpl sessionFactory = new SessionFactoryImpl(DB.getDataSource(), metamodel);

    try (Session session = sessionFactory.openSession()) {
      List<TestEntity> result = session.createQuery(
          "WHERE id = :id",
          TestEntity.class)
        .setParameter("id", 1L)
        .getResult();

      assertNotNull(result);
      assertSame(result.getFirst(), session.getReference(TestEntity.class, 1L));
      assertSame(result.getFirst(), session.find(TestEntity.class, 1L));
    }
  }

  @Entity
  @Getter
  @ToString
  @NoArgsConstructor
  public static class TestEntity {

    @Id
    private Long id;
    @Column(name = "first_name", insertable = false)
    private String firstName;
    @Column(name = "last_name", updatable = false)
    private String lastName;
    @Column(nullable = false, unique = true)
    private String email;

  }

  @Test
  void testTheReferenceIsTheSameIfFindAndThanQuery() {
    MetamodelScanner metamodelScanner = new MetamodelScanner(new JdbcTypeProviderImpl());
    Metamodel metamodel = metamodelScanner.scanEntities(TestEntity.class);
    SessionFactoryImpl sessionFactory = new SessionFactoryImpl(DB.getDataSource(), metamodel);

    try (Session session = sessionFactory.openSession()) {
      TestEntity entityByFind = session.find(TestEntity.class, 1L);
      TestEntity entityByQuery = session.createQuery(
          "WHERE id = :id",
          TestEntity.class)
        .setParameter("id", 1L)
        .getResult().stream().findFirst().orElseThrow();

      assertSame(entityByFind, entityByQuery);
    }
  }

  @Test
  void testTheReferenceIsTheSameIfGetReferenceAndThanQuery() {
    MetamodelScanner metamodelScanner = new MetamodelScanner(new JdbcTypeProviderImpl());
    Metamodel metamodel = metamodelScanner.scanEntities(TestEntity.class);
    SessionFactoryImpl sessionFactory = new SessionFactoryImpl(DB.getDataSource(), metamodel);

    try (Session session = sessionFactory.openSession()) {
      TestEntity entityByFind = session.getReference(TestEntity.class, 1L);
      assertEquals(1L, entityByFind.getId());
      TestEntity entityByQuery = session.createQuery(
          "WHERE id = :id",
          TestEntity.class)
        .setParameter("id", 1L)
        .getResult().stream().findFirst().orElseThrow();

      assertSame(entityByFind, entityByQuery);
    }
  }

}