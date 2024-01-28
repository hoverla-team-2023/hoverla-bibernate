package com.bibernate.hoverla.generator;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

import javax.sql.DataSource;

import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.bibernate.hoverla.exceptions.BibernateSqlException;
import com.bibernate.hoverla.jdbc.PostgresSqlTestExtension;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SequenceGeneratorImplTest {

  private static final int THREAD_COUNT = 20;
  @RegisterExtension
  static PostgresSqlTestExtension DB = new PostgresSqlTestExtension("sequence/init-sequence-generator-test.sql", "sequence/clear-sequence-generator-test.sql");

  private SequenceGeneratorImpl sequenceGenerator = new SequenceGeneratorImpl("test_seq", 5);

  HikariDataSource dataSource = getHikariDataSource();

  /**
   * Inspired by <a href="https://vladmihalcea.com/race-condition/">The race condition test by Vlad Mihalcea</a>
   */
  @Test
  @Order(10)
  void whenTestingRaceCondition_thenValuesAreUniqueAndNoRedundantCalls() {

    // Create latches for synchronization
    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch endLatch = new CountDownLatch(THREAD_COUNT + 1);
    ConcurrentLinkedQueue<Long> generated = new ConcurrentLinkedQueue<>();

    // Create threads that generate sequence values concurrently
    for (int i = 0; i < THREAD_COUNT + 1; i++) {
      if (i == 10) {
        new Thread(() -> {
          awaitOnLatch(startLatch);

          inConnection(dataSource, connection -> {
            long next = sequenceGenerator.generateNextFromSequence(connection);
            log.info("Emulation concurrent sequence access from other service, generated value " + next);
          });

          endLatch.countDown();
        }).start();
      } else {
        new Thread(() -> {
          awaitOnLatch(startLatch);
          inConnection(dataSource, connection -> {
            long generatedValue = (long) sequenceGenerator.generateNext(connection);
            generated.add(generatedValue);
            log.info("Generated value: {}", generatedValue);
          });
          endLatch.countDown();
        }).start();
      }
    }

    log.info("Starting threads...");
    startLatch.countDown();

    log.info("Main thread waits for all transfer threads to finish...");
    awaitOnLatch(endLatch);
    long maxGeneratedValue = new HashSet<>(generated).stream().max(Long::compareTo).orElse(Long.MAX_VALUE);

    // verify number of unique generated values
    Assertions.assertEquals(THREAD_COUNT, new HashSet<>(generated).size());
    // verify that we don't have redundant db calls that leads to skipping values
    Assertions.assertEquals(25L, maxGeneratedValue);
    Assertions.assertTrue(maxGeneratedValue <= 25L);
  }

  @Test
  @Order(20)
  void whenSequenceDoesNotExist_thenBibernateExceptionIsThrown() {
    SequenceGeneratorImpl sequenceGenerator = new SequenceGeneratorImpl("non_existent_seq", 5);

    inConnection(dataSource, connection -> {
      Assertions.assertThrows(BibernateSqlException.class, () -> sequenceGenerator.generateNext(connection));
    });
  }

  private HikariDataSource getHikariDataSource() {
    HikariConfig hikariConfig = new HikariConfig();
    hikariConfig.setJdbcUrl(PostgresSqlTestExtension.POSTGRES_SQL_CONTAINER.getJdbcUrl());
    hikariConfig.setUsername(PostgresSqlTestExtension.POSTGRES_SQL_CONTAINER.getUsername());
    hikariConfig.setPassword(PostgresSqlTestExtension.POSTGRES_SQL_CONTAINER.getPassword());
    hikariConfig.setMaximumPoolSize(10);
    return new HikariDataSource(hikariConfig);
  }

  private void inConnection(DataSource dataSource, Consumer<Connection> consumer) {
    try (Connection connection = dataSource.getConnection()) {
      consumer.accept(connection);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  private void awaitOnLatch(CountDownLatch latch) {
    try {
      latch.await();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

}