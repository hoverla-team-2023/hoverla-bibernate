package com.bibernate.hoverla.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.tree.ParseTree;

import com.bibernate.grammar.WhereStatementLexer;
import com.bibernate.grammar.WhereStatementParser;
import com.bibernate.hoverla.exceptions.BibernateBqlException;
import com.bibernate.hoverla.exceptions.BibernateException;
import com.bibernate.hoverla.metamodel.EntityMapping;
import com.bibernate.hoverla.metamodel.Metamodel;
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

    final List<String> errorMessages = new ArrayList<>();
    lexer.addErrorListener(new BaseErrorListener() {
      @Override
      public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
        errorMessages.add("line " + line + ":" + charPositionInLine + " " + msg);
      }
    });

    ParseTree tree = parser.start();

    int numberOfSyntaxErrors = parser.getNumberOfSyntaxErrors();
    if (numberOfSyntaxErrors > 0 || !errorMessages.isEmpty()) {
      throw new BibernateBqlException(String.join(",", errorMessages));
    }
    return tree;
  }

  /**
   * Retrieves the entity key from the given entity object using the provided metamodel.
   *
   * @param entityClass the class of the entity object
   * @param entity      the entity object
   * @param metamodel   the metamodel that represents scanned entities with their metadata
   *
   * @return the entity key
   *
   * @throws BibernateException if there is an error getting the entity key
   */
  public static EntityKey getEntityKeyFromEntity(Class<?> entityClass, Object entity, Metamodel metamodel) {
    EntityMapping entityMapping = metamodel.getEntityMappingMap().get(entityClass);
    String primaryKeyFieldName = entityMapping.getPrimaryKeyMapping().getFieldName();
    try {
      Field declaredField = entityClass.getDeclaredField(primaryKeyFieldName);
      declaredField.setAccessible(true);
      Object object = declaredField.get(entity);
      return new EntityKey(entityClass, object);
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

}
