package com.bibernate.hoverla.generator;

import java.sql.Connection;

/**
 * The Generator interface represents a generic generator that can be used to generate unique values or identifiers.
 */
public interface Generator {

  /**
   * Generates the next unique value or identifier.
   *
   * @param connection A database connection (may be unused, depending on the implementation).
   *
   * @return The next unique value or identifier.
   */
  Object generateNext(Connection connection);

}
