package com.bibernate.hoverla.session;

import java.util.function.Consumer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.bibernate.hoverla.annotations.Column;
import com.bibernate.hoverla.annotations.Entity;
import com.bibernate.hoverla.annotations.Id;
import com.bibernate.hoverla.annotations.ManyToOne;
import com.bibernate.hoverla.annotations.SequenceGeneratedValue;
import com.bibernate.hoverla.session.cache.EntityEntry;
import com.bibernate.hoverla.session.cache.EntityKey;
import com.bibernate.hoverla.session.cache.PersistenceContext;
import com.bibernate.hoverla.session.dirtycheck.DirtyCheckService;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ExtendWith(MockitoExtension.class)
public class PersistenceContextTest {

  @Mock
  private SessionImplementor sessionImplementor;

  @Mock
  private DirtyCheckService dirtyCheckService;

  private PersistenceContext persistenceContext;

  @BeforeEach
  void setUp() {
    persistenceContext = new PersistenceContext(sessionImplementor, dirtyCheckService);
  }

  @Test
  void manageEntity() {
    EntityKey<TestEntity> entityKey = new EntityKey<>(TestEntity.class, 1L);
    TestEntity testEntity = new TestEntity();
    testEntity.setId(1L);
    testEntity.setName("test");

    Consumer<EntityEntry> processFunction = entityEntry -> {
      Assertions.assertNotNull(entityEntry.getEntity());
    };

    EntityEntry entityEntry = persistenceContext.manageEntity(entityKey, () -> testEntity, processFunction);

    Assertions.assertNotNull(entityEntry);
    Assertions.assertNotNull(entityEntry.getEntity());

    persistenceContext.removeEntity(entityKey);
  }

  @Test
  public void getEntityEntry_withNullEntityKey() {
    EntityEntry result = persistenceContext.getEntityEntry(null);
    Assertions.assertNull(result);
  }

  @Test
  void getEntityEntry() {
    EntityKey<TestEntity> entityKey = new EntityKey<>(TestEntity.class, 1L);
    TestEntity testEntity = new TestEntity();
    testEntity.setId(1L);
    testEntity.setName("test");

    persistenceContext.manageEntity(entityKey, () -> testEntity, entityEntry -> {});

    EntityEntry entityEntry = persistenceContext.getEntityEntry(entityKey);

    Assertions.assertNotNull(entityEntry);
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
    SessionITest.TestComment comment;
    @Getter
    @Setter
    private String name;

  }

}