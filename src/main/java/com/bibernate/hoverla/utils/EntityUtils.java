package com.bibernate.hoverla.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ConsoleErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.tree.ParseTree;

import com.bibernate.grammar.WhereStatementLexer;
import com.bibernate.grammar.WhereStatementParser;
import com.bibernate.hoverla.exceptions.BibernateBqlException;
import com.bibernate.hoverla.exceptions.BibernateException;
import com.bibernate.hoverla.jdbc.types.BibernateJdbcType;
import com.bibernate.hoverla.metamodel.EntityMapping;
import com.bibernate.hoverla.metamodel.FieldMapping;
import com.bibernate.hoverla.session.cache.EntityKey;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EntityUtils {

  /**
   * Converts a camel case name to a snake case name with a first letter in the lower case.
   * For example, "firstName" becomes "first_name", "BookAuthors" becomes "book_authors".
   *
   * @param value the name to convert
   *
   * @return the converted name in snake case
   */
  public static String toSnakeCase(String value) {
    if (value == null) {
      return null;
    }
    return value.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
  }

  /**
   * Parses a WHERE statement using the provided input string and returns the parse tree.
   *
   * @param whereStatement The input string containing the WHERE statement.
   *
   * @return The parse tree representing the parsed WHERE statement.
   *
   * @throws BibernateBqlException If there is a syntax error in the input or if there are multiple
   *                               syntax errors, an exception is thrown with error details.
   */
  public static ParseTree parseWhereStatement(String whereStatement) {
    WhereStatementLexer lexer = new WhereStatementLexer(CharStreams.fromString(whereStatement));
    WhereStatementParser parser = new WhereStatementParser(new CommonTokenStream(lexer));

    final List<String> errorMessages = listenErrorMessages(parser);

    ParseTree tree = parser.start();
    validateSyntaxErrors(parser, errorMessages);

    return tree;
  }
  /**
   * This private static method validates the syntax errors in a WhereStatementParser.
   * It checks the number of syntax errors and the error messages list.
   * If there are any syntax errors or error messages, it throws a BibernateBqlException with a combined error message.
   *
   * @param parser The WhereStatementParser to validate.
   * @param errorMessages The list of error messages to validate.
   * @throws BibernateBqlException If there are any syntax errors or error messages.
   */
  private static void validateSyntaxErrors(WhereStatementParser parser, List<String> errorMessages) {
    int numberOfSyntaxErrors = parser.getNumberOfSyntaxErrors();
    if (numberOfSyntaxErrors > 0 || !errorMessages.isEmpty()) {
      throw new BibernateBqlException(String.join(",", errorMessages));
    }
  }
  /**
   * This private static method listens for error messages in a WhereStatementParser.
   * It removes the default ConsoleErrorListener and adds a custom error listener that collects syntax errors.
   * The syntax errors are collected in a list of error messages, which includes the line number, character position, and error message.
   *
   * @param parser The WhereStatementParser to listen for errors.
   * @return A list of error messages collected from the parser.
   */
  private static List<String> listenErrorMessages(WhereStatementParser parser) {
    final List<String> errorMessages = new ArrayList<>();
    parser.removeErrorListener(ConsoleErrorListener.INSTANCE);
    parser.addErrorListener(new BaseErrorListener() {
      @Override
      public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
        errorMessages.add("line " + line + ":" + charPositionInLine + " " + msg);
      }
    });
    return errorMessages;
  }

  /**
   * Retrieves an {@link EntityKey} for the specified entity based on its primary key field.
   *
   * @param entityClass         The class of the entity.
   * @param entity              The entity instance from which to retrieve the primary key.
   * @param primaryKeyFieldName The name of the primary key field in the entity class.
   *
   * @return An {@link EntityKey} representing the entity's primary key.
   *
   * @throws BibernateException If an error occurs while attempting to retrieve the entity key.
   */
  public static <T> EntityKey<T> getEntityKey(Class<T> entityClass, T entity, String primaryKeyFieldName) {
    try {
      Field declaredField = entityClass.getDeclaredField(primaryKeyFieldName);
      declaredField.setAccessible(true);
      Object object = declaredField.get(entity);
      return new EntityKey<>(entityClass, object);
    } catch (IllegalAccessException | NoSuchFieldException exc) {
      throw new BibernateException("Failed to get entity key for: " + entityClass, exc);
    }
  }

  public static <T> T newInstanceOf(Class<T> entityType) {
    try {
      return entityType.getDeclaredConstructor().newInstance();
    } catch (InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException exc) {
      throw new BibernateException(String.format("Error instantiating object of type %s. Each entity must have a default no-args constructor",
                                                 entityType.getName()), exc);
    }
  }
  /**
   * This static method sets the value of a field in an entity.
   * The field is identified by its name and the entity is provided as an object.
   * The method uses reflection to access and modify the field.
   *
   * @param fieldName The name of the field to set the value for.
   * @param entity The entity instance whose field value will be set.
   * @param value The value to be set in the field.
   * @throws BibernateException If the field does not exist or cannot be accessed or modified due to security restrictions.
   */
  public static void setFieldValue(String fieldName, Object entity, Object value) {
    try {
      Field field = entity.getClass().getDeclaredField(fieldName);
      field.setAccessible(true);
      field.set(entity, value);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new BibernateException(String.format(
        "Error setting value to field %s of entity %s",
        fieldName,
        entity.getClass().getName())
      );
    }
  }
  /**
   * This static method retrieves the value of a field from an entity.
   * The field is identified by its name and the entity is provided as an object.
   * The method uses reflection to access and retrieve the field value.
   *
   * @param fieldName The name of the field to get the value from.
   * @param entity The entity instance whose field value will be retrieved.
   * @return The value of the field in the entity.
   * @throws BibernateException If the field does not exist or cannot be accessed due to security restrictions.
   */
  public static Object getFieldValue(String fieldName, Object entity) {
    try {
      Field field = entity.getClass().getDeclaredField(fieldName);
      field.setAccessible(true);
      return field.get(entity);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new BibernateException(String.format(
        "Error getting value from field %s of entity %s",
        fieldName,
        entity.getClass().getName())
      );
    }
  }

}
