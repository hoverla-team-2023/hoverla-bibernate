package com.bibernate.hoverla.utils;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

  @ParameterizedTest
  @MethodSource("toSnakeCaseTestingValues")
  void toSnakeCase(String value, String expected) {
    String actual = EntityUtils.toSnakeCase(value);

    assertEquals(expected, actual);
  }

}
