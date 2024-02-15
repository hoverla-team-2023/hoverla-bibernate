package com.bibernate.hoverla.session;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.bibernate.hoverla.annotations.Column;
import com.bibernate.hoverla.annotations.Entity;
import com.bibernate.hoverla.annotations.Id;
import com.bibernate.hoverla.annotations.IdentityGeneratedValue;
import com.bibernate.hoverla.annotations.OptimisticLock;
import com.bibernate.hoverla.annotations.Table;
import com.bibernate.hoverla.exceptions.InvalidEntityDeclarationException;
import com.bibernate.hoverla.jdbc.PostgresSqlTestExtension;
import com.bibernate.hoverla.jdbc.types.provider.JdbcTypeProviderImpl;
import com.bibernate.hoverla.metamodel.scan.MetamodelScanner;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OptimisticLockITest {

  @RegisterExtension
  static PostgresSqlTestExtension DB = new PostgresSqlTestExtension("session-optimistic-lock/init.sql", "session-test/clear.sql");

  private MetamodelScanner metamodelScanner;
  private SessionFactory sessionFactory;

  @BeforeEach
  void setup() {
    this.metamodelScanner = new MetamodelScanner(new JdbcTypeProviderImpl());
    var metamodel = this.metamodelScanner.scanEntities(
      TestEntityPrimitiveIntOptimisticLock.class,
      TestEntityIntegerOptimisticLock.class,
      TestEntityLongOptimisticLock.class,
      TestEntityPrimitiveLongOptimisticLock.class
    );
    this.sessionFactory = new SessionFactoryImpl(DB.getDataSource(), metamodel);
  }

  @Test
  @Order(10)
  @SneakyThrows
  void updateEntityWithPrimitiveIntOptimisticLock_updateDb() {
    long primaryKey = 1L;
    var connection = DB.getDataSource().getConnection();
    String newFirstName = "Johnny";
    String newLastName = "Cooper";
    int expectedNextLockVersion = 2;

    var recordBeforeUpdate = selectEntityWithPrimitiveIntLock(connection, primaryKey);
    assertNotNull(recordBeforeUpdate);
    assertEquals(1, recordBeforeUpdate.getVersion());

    TestEntityPrimitiveIntOptimisticLock entity = sessionFactory.fromTransaction(session -> {
      TestEntityPrimitiveIntOptimisticLock testEntity = session.getReference(TestEntityPrimitiveIntOptimisticLock.class, primaryKey);
      testEntity.setFirstName(newFirstName);
      testEntity.setLastName(newLastName);

      return testEntity;
    });

    assertEquals(expectedNextLockVersion, entity.getVersion());

    var recordAfterUpdate = selectEntityWithPrimitiveIntLock(connection, primaryKey);
    assertNotNull(recordAfterUpdate);
    assertEquals(newFirstName, recordAfterUpdate.getFirstName());
    assertEquals(newLastName, recordAfterUpdate.getLastName());
    assertEquals(expectedNextLockVersion, recordAfterUpdate.getVersion());
  }

  @Test
  @Order(20)
  @SneakyThrows
  void updateEntityWithIntegerOptimisticLock_updateDb() {
    long primaryKey = 2L;
    var connection = DB.getDataSource().getConnection();
    String newFirstName = "Samuel";
    String newLastName = "Jackson";
    int expectedNextLockVersion = 3;

    var recordBeforeUpdate = selectEntityWithIntegerLock(connection, primaryKey);
    assertNotNull(recordBeforeUpdate);
    assertEquals(2, recordBeforeUpdate.getVersion());

    TestEntityIntegerOptimisticLock entity = sessionFactory.fromTransaction(session -> {
      TestEntityIntegerOptimisticLock testEntity = session.find(TestEntityIntegerOptimisticLock.class, primaryKey);
      testEntity.setFirstName(newFirstName);
      testEntity.setLastName(newLastName);

      return testEntity;
    });

    assertEquals(expectedNextLockVersion, entity.getVersion());

    var recordAfterUpdate = selectEntityWithIntegerLock(connection, primaryKey);
    assertNotNull(recordAfterUpdate);
    assertEquals(newFirstName, recordAfterUpdate.getFirstName());
    assertEquals(newLastName, recordAfterUpdate.getLastName());
    assertEquals(expectedNextLockVersion, recordAfterUpdate.getVersion());
  }

  @Test
  @Order(30)
  @SneakyThrows
  void updateEntityWithPrimitiveLongOptimisticLock_updateDb() {
    long primaryKey = 2L;
    var connection = DB.getDataSource().getConnection();
    String newFirstName = "Nathan";
    String newLastName = "Drake";
    long expectedNextLockVersion = 4L;

    var recordBeforeUpdate = selectEntityWithPrimitiveLongLock(connection, primaryKey);
    assertNotNull(recordBeforeUpdate);
    assertEquals(3, recordBeforeUpdate.getVersion());

    TestEntityPrimitiveLongOptimisticLock entity = sessionFactory.fromTransaction(session -> {
      TestEntityPrimitiveLongOptimisticLock testEntity = session.find(TestEntityPrimitiveLongOptimisticLock.class, primaryKey);
      testEntity.setFirstName(newFirstName);
      testEntity.setLastName(newLastName);

      return testEntity;
    });

    assertEquals(expectedNextLockVersion, entity.getVersion());

    var recordAfterUpdate = selectEntityWithPrimitiveLongLock(connection, primaryKey);
    assertNotNull(recordAfterUpdate);
    assertEquals(newFirstName, recordAfterUpdate.getFirstName());
    assertEquals(newLastName, recordAfterUpdate.getLastName());
    assertEquals(expectedNextLockVersion, recordAfterUpdate.getVersion());
  }

  @Test
  @Order(40)
  @SneakyThrows
  void updateEntityWithLongOptimisticLock_updateDb() {
    long primaryKey = 1L;
    var connection = DB.getDataSource().getConnection();
    String newFirstName = "Ada";
    String newLastName = "Lovelace";
    long expectedNextLockVersion = 11L;

    var recordBeforeUpdate = selectEntityWithLongLock(connection, primaryKey);
    assertNotNull(recordBeforeUpdate);
    assertEquals(10, recordBeforeUpdate.getVersion());

    TestEntityLongOptimisticLock entity = sessionFactory.fromTransaction(session -> {
      TestEntityLongOptimisticLock testEntity = session.getReference(TestEntityLongOptimisticLock.class, primaryKey);
      testEntity.setFirstName(newFirstName);
      testEntity.setLastName(newLastName);
      return testEntity;
    });

    assertEquals(expectedNextLockVersion, entity.getVersion());

    var recordAfterUpdate = selectEntityWithLongLock(connection, primaryKey);
    assertNotNull(recordAfterUpdate);
    assertEquals(newFirstName, recordAfterUpdate.getFirstName());
    assertEquals(newLastName, recordAfterUpdate.getLastName());
    assertEquals(expectedNextLockVersion, recordAfterUpdate.getVersion());
  }

  @Test
  @Order(50)
  @SneakyThrows
  void persistEntityWithPrimitiveIntOptimisticLock_insertIntoDb() {
    int expectedPrimaryKey = 3;
    String firstName = "John";
    String lastName = "Doe";
    int expectedOptimisticLockVersion = 1;

    TestEntityPrimitiveIntOptimisticLock persistedEntity = sessionFactory.fromTransaction(session -> {
      TestEntityPrimitiveIntOptimisticLock entity = new TestEntityPrimitiveIntOptimisticLock(firstName, lastName);
      session.persist(entity);

      return entity;
    });

    assertEquals(expectedPrimaryKey, persistedEntity.getId());
    assertEquals(expectedOptimisticLockVersion, persistedEntity.getVersion());

    var connection = DB.getDataSource().getConnection();
    var recordAfterInsert = selectEntityWithPrimitiveIntLock(connection, expectedPrimaryKey);
    assertNotNull(recordAfterInsert);
    assertEquals(expectedPrimaryKey, recordAfterInsert.getId());
    assertEquals(firstName, recordAfterInsert.getFirstName());
    assertEquals(lastName, recordAfterInsert.getLastName());
    assertEquals(expectedOptimisticLockVersion, recordAfterInsert.getVersion());
  }

  @Test
  @Order(60)
  @SneakyThrows
  void persistEntityWithIntegerOptimisticLock_insertIntoDb() {
    int expectedPrimaryKey = 3;
    String firstName = "John";
    String lastName = "Doe";
    Integer expectedOptimisticLockVersion = 1;

    TestEntityIntegerOptimisticLock persistedEntity = sessionFactory.fromTransaction(session -> {
      TestEntityIntegerOptimisticLock entity = new TestEntityIntegerOptimisticLock(firstName, lastName);
      session.persist(entity);

      return entity;
    });

    assertEquals(expectedPrimaryKey, persistedEntity.getId());
    assertEquals(expectedOptimisticLockVersion, persistedEntity.getVersion());

    var connection = DB.getDataSource().getConnection();
    var recordAfterInsert = selectEntityWithIntegerLock(connection, expectedPrimaryKey);
    assertNotNull(recordAfterInsert);
    assertEquals(expectedPrimaryKey, recordAfterInsert.getId());
    assertEquals(firstName, recordAfterInsert.getFirstName());
    assertEquals(lastName, recordAfterInsert.getLastName());
    assertEquals(expectedOptimisticLockVersion, recordAfterInsert.getVersion());
  }

  @Test
  @Order(70)
  @SneakyThrows
  void persistEntityWithPrimitiveLongOptimisticLock_insertIntoDb() {
    int expectedPrimaryKey = 3;
    String firstName = "John";
    String lastName = "Doe";
    long expectedOptimisticLockVersion = 1L;

    TestEntityPrimitiveLongOptimisticLock persistedEntity = sessionFactory.fromTransaction(session -> {
      TestEntityPrimitiveLongOptimisticLock entity = new TestEntityPrimitiveLongOptimisticLock(firstName, lastName);
      session.persist(entity);

      return entity;
    });

    assertEquals(expectedPrimaryKey, persistedEntity.getId());
    assertEquals(expectedOptimisticLockVersion, persistedEntity.getVersion());

    var connection = DB.getDataSource().getConnection();
    var recordAfterInsert = selectEntityWithPrimitiveLongLock(connection, expectedPrimaryKey);
    assertNotNull(recordAfterInsert);
    assertEquals(expectedPrimaryKey, recordAfterInsert.getId());
    assertEquals(firstName, recordAfterInsert.getFirstName());
    assertEquals(lastName, recordAfterInsert.getLastName());
    assertEquals(expectedOptimisticLockVersion, recordAfterInsert.getVersion());
  }

  @Test
  @Order(80)
  @SneakyThrows
  void persistEntityWithLongOptimisticLock_insertIntoDb() {
    int expectedPrimaryKey = 3;
    String firstName = "John";
    String lastName = "Doe";
    Long expectedOptimisticLockVersion = 1L;

    TestEntityLongOptimisticLock persistedEntity = sessionFactory.fromTransaction(session -> {
      TestEntityLongOptimisticLock entity = new TestEntityLongOptimisticLock(firstName, lastName);
      session.persist(entity);

      return entity;
    });

    assertEquals(expectedPrimaryKey, persistedEntity.getId());
    assertEquals(expectedOptimisticLockVersion, persistedEntity.getVersion());

    var connection = DB.getDataSource().getConnection();
    var recordAfterInsert = selectEntityWithLongLock(connection, expectedPrimaryKey);
    assertNotNull(recordAfterInsert);
    assertEquals(expectedPrimaryKey, recordAfterInsert.getId());
    assertEquals(firstName, recordAfterInsert.getFirstName());
    assertEquals(lastName, recordAfterInsert.getLastName());
    assertEquals(expectedOptimisticLockVersion, recordAfterInsert.getVersion());
  }

  @Test
  @Order(90)
  void entityWithObjectOptimisticLock_throwException() {
    Class<TestEntityObjectOptimisticLock> entityClass = TestEntityObjectOptimisticLock.class;
    String expectedMessage = String.format(
      "Entity '%s' has an optimistic lock field '%s' of type '%s' which is not supported. Please use Integer or Long",
      entityClass.getName(), "version", Object.class
    );

    var resultException = assertThrows(InvalidEntityDeclarationException.class, () -> metamodelScanner.scanEntities(entityClass));
    assertEquals(expectedMessage, resultException.getMessage());
  }

  @Test
  @Order(100)
  void entityWithMultipleOptimisticLocks_throwException() {
    Class<TestEntityMultipleOptimisticLocks> entityClass = TestEntityMultipleOptimisticLocks.class;
    String expectedMessage = String.format(
      "Entity '%s' has multiple optimistic lock fields defined. Please define at most one field with @OptimisticLock annotation",
      entityClass.getName()
    );

    var resultException = assertThrows(InvalidEntityDeclarationException.class, () -> metamodelScanner.scanEntities(entityClass));
    assertEquals(expectedMessage, resultException.getMessage());
  }

  private TestEntityPrimitiveIntOptimisticLock selectEntityWithPrimitiveIntLock(Connection connection, long primaryKey) throws SQLException {
    var statement = connection.prepareStatement("SELECT * FROM entity_primitive_int_lock WHERE id=?;");
    statement.setLong(1, primaryKey);

    var resultSet = statement.executeQuery();

    if (resultSet.next()) {
      long id = resultSet.getLong("id");
      String firstName = resultSet.getString("first_name");
      String lastName = resultSet.getString("last_name");
      int version = resultSet.getInt("version");

      return new TestEntityPrimitiveIntOptimisticLock(id, firstName, lastName, version);
    }
    return null;
  }

  private TestEntityIntegerOptimisticLock selectEntityWithIntegerLock(Connection connection, long primaryKey) throws SQLException {
    var statement = connection.prepareStatement("SELECT * FROM entity_integer_lock WHERE id=?;");
    statement.setLong(1, primaryKey);

    var resultSet = statement.executeQuery();

    if (resultSet.next()) {
      long id = resultSet.getLong("id");
      String firstName = resultSet.getString("first_name");
      String lastName = resultSet.getString("last_name");
      int version = resultSet.getInt("version");

      return new TestEntityIntegerOptimisticLock(id, firstName, lastName, version);
    }
    return null;
  }

  private TestEntityPrimitiveLongOptimisticLock selectEntityWithPrimitiveLongLock(Connection connection, long primaryKey) throws SQLException {
    var statement = connection.prepareStatement("SELECT * FROM entity_primitive_long_lock WHERE id=?;");
    statement.setLong(1, primaryKey);

    var resultSet = statement.executeQuery();

    if (resultSet.next()) {
      long id = resultSet.getLong("id");
      String firstName = resultSet.getString("first_name");
      String lastName = resultSet.getString("last_name");
      long version = resultSet.getLong("version");

      return new TestEntityPrimitiveLongOptimisticLock(id, firstName, lastName, version);
    }
    return null;
  }

  private TestEntityLongOptimisticLock selectEntityWithLongLock(Connection connection, long primaryKey) throws SQLException {
    var statement = connection.prepareStatement("SELECT * FROM entity_long_lock WHERE id=?;");
    statement.setLong(1, primaryKey);

    var resultSet = statement.executeQuery();

    if (resultSet.next()) {
      long id = resultSet.getLong("id");
      String firstName = resultSet.getString("first_name");
      String lastName = resultSet.getString("last_name");
      long version = resultSet.getLong("version");

      return new TestEntityLongOptimisticLock(id, firstName, lastName, version);
    }
    return null;
  }

  @Data
  @Entity
  @Table("entity_primitive_int_lock")
  @AllArgsConstructor
  @NoArgsConstructor
  public static class TestEntityPrimitiveIntOptimisticLock {

    @Id
    @IdentityGeneratedValue
    private Long id;
    @Column(name = "first_name")
    private String firstName;
    @Column(name = "last_name")
    private String lastName;
    @OptimisticLock
    private int version;

    public TestEntityPrimitiveIntOptimisticLock(String firstName, String lastName) {
      this.firstName = firstName;
      this.lastName = lastName;
    }

  }

  @Data
  @Entity
  @Table("entity_integer_lock")
  @AllArgsConstructor
  @NoArgsConstructor
  public static class TestEntityIntegerOptimisticLock {

    @Id
    @IdentityGeneratedValue
    private Long id;
    private String firstName;
    private String lastName;
    @OptimisticLock
    private Integer version;

    public TestEntityIntegerOptimisticLock(String firstName, String lastName) {
      this.firstName = firstName;
      this.lastName = lastName;
    }

  }

  @Data
  @Entity
  @Table("entity_primitive_long_lock")
  @AllArgsConstructor
  @NoArgsConstructor
  public static class TestEntityPrimitiveLongOptimisticLock {

    @Id
    @IdentityGeneratedValue
    private Long id;
    private String firstName;
    private String lastName;
    @OptimisticLock
    private long version;

    public TestEntityPrimitiveLongOptimisticLock(String firstName, String lastName) {
      this.firstName = firstName;
      this.lastName = lastName;
    }

  }

  @Data
  @Entity
  @Table("entity_long_lock")
  @AllArgsConstructor
  @NoArgsConstructor
  public static class TestEntityLongOptimisticLock {

    @Id
    @IdentityGeneratedValue
    private Long id;
    @Column(name = "first_name")
    private String firstName;
    @Column(name = "last_name")
    private String lastName;
    @OptimisticLock
    private Long version;

    public TestEntityLongOptimisticLock(String firstName, String lastName) {
      this.firstName = firstName;
      this.lastName = lastName;
    }

  }

  @Data
  @Entity
  @AllArgsConstructor
  @NoArgsConstructor
  public static class TestEntityObjectOptimisticLock {

    @Id
    @IdentityGeneratedValue
    private Long id;
    @Column(name = "first_name")
    private String firstName;
    @Column(name = "last_name")
    private String lastName;
    @OptimisticLock
    private Object version;

  }

  @Data
  @Entity
  @AllArgsConstructor
  @NoArgsConstructor
  public static class TestEntityMultipleOptimisticLocks {

    @Id
    @IdentityGeneratedValue
    private Long id;
    @Column(name = "first_name")
    private String firstName;
    @Column(name = "last_name")
    private String lastName;
    @OptimisticLock
    private int v1;
    @OptimisticLock
    private long v2;

  }

}
