package com.bibernate.hoverla.utils;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.bibernate.hoverla.exceptions.BibernateBqlException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class EntityUtilsTest {

  static Stream<Arguments> toSnakeCaseTestingValues() {
    return Stream.of(
      arguments("firstName", "first_name"),
      arguments("BookAuthors", "book_authors"),
      arguments("", ""),
      arguments(null, null)
    );
  }

  static Stream<Arguments> parseWhereStatementValidValues() {
    return Stream.of(
      arguments("WHERE firstName = :name"),
      arguments("WHERE firstName = :name AND lastName = :lastName"),
      arguments("WHERE (firstName = :name AND age > :age) OR (city = :city AND state = :state)"),
      arguments("WHERE (firstName = :name AND age < :age) OR (city = :city AND state = :state)"),
      arguments("WHERE age >= :minAge AND age <= :maxAge"),
      arguments("WHERE department IN :departments")
    );
  }

  static Stream<Arguments> parseWhereStatementInValidValues() {
    return Stream.of(
      arguments("WHERE :name = firstName"),
      arguments("WHEREE :name = firstName"),
      arguments("WHERE "),
      arguments("WHERE (city = :city AND state = :state")
    );
  }

  @ParameterizedTest
  @MethodSource("toSnakeCaseTestingValues")
  void toSnakeCase(String value, String expected) {
    String actual = EntityUtils.toSnakeCase(value);

    assertEquals(expected, actual);
  }

  @ParameterizedTest
  @MethodSource("parseWhereStatementValidValues")
  void testParseWhereStatementValidValues_noErrorIsThrow(String value) {
    assertDoesNotThrow(() -> EntityUtils.parseWhereStatement(value));
  }

  @ParameterizedTest
  @MethodSource("parseWhereStatementInValidValues")
  void testParseWhereStatementInValidValues_noErrorIsThrow(String value) {
    assertThrows(BibernateBqlException.class, () -> EntityUtils.parseWhereStatement(value));
  }

}
