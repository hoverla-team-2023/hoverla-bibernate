package com.bibernate.hoverla.generator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import com.bibernate.hoverla.exceptions.BibernateSqlException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * An implementation of a thread-safe sequence generator for PostgreSQL.
 * This generator ensures thread safety while generating unique values from a PostgreSQL sequence.
 * <p/>
 * It is crucial to align the <em>allocation size</em> with the <em>database sequence's increment
 * value</em> to guarantee correct behavior.
 * <p/>
 * This class uses locks for two important reasons:
 * <ol>
 * <li> To ensure that the initial value is allocated only once, preventing multiple threads
 * from attempting simultaneous allocation.</li>
 * <li> To prevent multiple threads from making redundant database calls within a race condition,
 * ensuring correct behavior and maintaining thread safety.</li>
 * </ol>
 */
@Slf4j
@RequiredArgsConstructor
public class SequenceGeneratorImpl implements Generator {

  private static final String SEQUENCE_SQL_REQUEST = "SELECT nextval(?);";

  private final String sequenceName;
  private final int allocationSize;

  private final AtomicLong currentVal = new AtomicLong();
  private volatile Long firstAllocatedValue;
  private volatile boolean isInitialized;

  /**
   * Generates the next unique value from the PostgreSQL sequence.
   *
   * @param connection The database connection to use for generating the value.
   *
   * @return The next unique value.
   */
  @Override
  public Object generateNext(Connection connection) {
    log.debug("Generating next value from sequence: {}, allocation size: {}...", sequenceName, allocationSize);

    if (!isInitialized) {
      synchronized (this) {
        if (!isInitialized) {
          log.debug("Allocating initial value for sequence: {}...", sequenceName);
          firstAllocatedValue = generateNextFromSequence(connection);
          log.debug("Allocated initial value :{} for sequence: {} ", firstAllocatedValue, sequenceName);
          currentVal.set(firstAllocatedValue);
          isInitialized = true;
          return currentVal.getAndIncrement();
        }
      }
    }
    getNextFromSequenceIfAllocationExhausted(connection);
    return currentVal.getAndIncrement();
  }

  /**
   * Checks if the allocation of values has been exhausted and fetches the next value from the
   * database if needed.
   *
   * @param connection The database connection to use for fetching the next value.
   */
  private void getNextFromSequenceIfAllocationExhausted(Connection connection) {
    if (Objects.equals((currentVal.get()) % allocationSize, firstAllocatedValue % allocationSize)) {
      synchronized (this) {
        if (Objects.equals((currentVal.get()) % allocationSize, firstAllocatedValue % allocationSize)) {
          log.debug("Allocating more values from the sequence: {}", sequenceName);

          long next = generateNextFromSequence(connection);

          log.info("Generated next value from sequence: {}, next value {}", sequenceName, next);

          currentVal.set(next);
        }
      }
    }
  }

  /**
   * Fetches the next value from the PostgreSQL sequence.
   *
   * @param connection The database connection to use for fetching the next value.
   *
   * @return The next value from the sequence.
   *
   * @throws BibernateSqlException If there is an error while retrieving the next value from the sequence.
   */
  protected long generateNextFromSequence(Connection connection) {
    log.debug("Executing SQL query to retrieve next value from sequence: {}", sequenceName);
    try (PreparedStatement preparedStatement = connection.prepareStatement(SEQUENCE_SQL_REQUEST)) {
      preparedStatement.setObject(1, sequenceName);
      ResultSet rs = preparedStatement.executeQuery();
      if (rs.next()) {
        return rs.getLong(1);
      } else {
        throw new BibernateSqlException("Failed to retrieve the next values from the sequence:" + sequenceName);
      }
    } catch (SQLException exception) {
      throw new BibernateSqlException("Failed to retrieve the next values from the sequence:" + sequenceName, exception);
    }
  }

}
