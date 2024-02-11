package com.bibernate.hoverla.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.bibernate.grammar.WhereStatementBaseVisitor;
import com.bibernate.grammar.WhereStatementParser;
import com.bibernate.hoverla.exceptions.BibernateBqlException;
import com.bibernate.hoverla.exceptions.BibernateBqlInvalidParameterException;
import com.bibernate.hoverla.exceptions.BibernateBqlMissingParameterException;
import com.bibernate.hoverla.jdbc.JdbcParameterBinding;
import com.bibernate.hoverla.metamodel.FieldMapping;
import com.bibernate.hoverla.metamodel.Metamodel;

import lombok.extern.slf4j.Slf4j;

import static com.bibernate.hoverla.jdbc.JdbcParameterBinding.bindParameter;

/**
 * A visitor class for parsing and generating SQL expressions from a WhereStatement grammar parse tree.
 * <p/>
 * This class extends the {@link WhereStatementBaseVisitor} provided by ANTLR4.
 * It is used to translate object-oriented query language expressions into SQL expression.
 * <p/>
 * The WhereStatement grammar defines a set of rules for creating structured queries with logical and comparison
 * operators, grouping with parentheses, and parameterized placeholders. This visitor class enables the transformation
 * of these structured query expressions into valid SQL queries.</p>
 */
@Slf4j
public class BibernateWhereStatementVisitor extends WhereStatementBaseVisitor<String> {

  private final Metamodel metamodel;
  private final Class<?> entityClass;
  private final Map<String, Object> parameters;

  private final List<JdbcParameterBinding<?>> orderedJdbcParameters;

  public BibernateWhereStatementVisitor(Metamodel metamodel,
                                        Class<?> entityClass,
                                        Map<String, Object> parameters) {
    super();
    this.metamodel = metamodel;
    this.entityClass = entityClass;
    this.parameters = parameters;
    this.orderedJdbcParameters = new ArrayList<>();
  }

  @Override
  public String visitAndPredicate(WhereStatementParser.AndPredicateContext ctx) {
    String left = visit(ctx.expression(0));
    String right = visit(ctx.expression(1));
    log.trace("Visiting AND predicate: {} AND {}", left, right);
    return left + " AND " + right;
  }

  @Override
  public String visitOrPredicate(WhereStatementParser.OrPredicateContext ctx) {
    String left = visit(ctx.expression(0));
    String right = visit(ctx.expression(1));
    log.trace("Visiting OR predicate: {} OR {}", left, right);
    return left + " OR " + right;
  }

  @Override
  public String visitParenExpession(WhereStatementParser.ParenExpessionContext ctx) {
    String expression = visit(ctx.expression());
    log.trace("Visiting Parenthesized Expression: ({})", expression);
    return "(" + expression + ")";
  }

  /**
   * Visits an IN predicate context in the WhereStatementParser and generates SQL for it.
   *
   * @param ctx The IN predicate context to visit.
   *
   * @return The generated SQL for the IN predicate. If the collection is empty or NULL, "TRUE" is returned,
   * indicating the IN condition is ignored due to no values to match.
   *
   * @throws BibernateBqlException If the parameter is not defined or is not a collection.
   */
  @Override
  public String visitInPredicate(WhereStatementParser.InPredicateContext ctx) {
    String parameterName = ctx.PARAMETER().getText().substring(1);
    validateParameterExistence(parameterName);

    var fieldMapping = getFieldMapping(ctx.IDENTIFIER().getText());
    var bindValue = parameters.get(parameterName);

    if (bindValue == null) {
      log.trace("IN Predicate: Parameter {} is NULL, returning TRUE", parameterName);
      return "TRUE";
    }

    if (!(bindValue instanceof Collection<?> collection)) {
      throw new BibernateBqlException("Parameter with name: %s is not collection".formatted(parameterName));
    }

    if (collection.isEmpty()) {
      log.trace("IN Predicate: Collection {} is empty, returning TRUE", parameterName);
      return "TRUE";
    }

    for (Object parameterValue : collection) {
      orderedJdbcParameters.add(bindParameter(parameterValue, fieldMapping.getJdbcType()));
    }
    return fieldMapping.getColumnName() + " IN (" + generatePlaceholders(collection) + ")";

  }

  @Override
  public String visitEqualsPredicate(WhereStatementParser.EqualsPredicateContext ctx) {
    String fieldName = ctx.IDENTIFIER().getText();
    String parameterName = ctx.PARAMETER().getText().substring(1);

    validateParameterExistence(parameterName);

    FieldMapping<?> fieldMapping = getFieldMapping(fieldName);

    orderedJdbcParameters.add(bindParameter(parameters.get(parameterName), fieldMapping.getJdbcType()));

    return fieldMapping.getColumnName() + " " + ctx.getChild(1).getText() + " ?";
  }

  @Override
  public String visitComparingPredicate(WhereStatementParser.ComparingPredicateContext ctx) {
    String fieldName = ctx.IDENTIFIER().getText();
    String parameterName = ctx.PARAMETER().getText().substring(1);

    validateParameterExistence(parameterName);

    FieldMapping<?> fieldMapping = getFieldMapping(fieldName);

    orderedJdbcParameters.add(bindParameter(parameters.get(parameterName), fieldMapping.getJdbcType()));

    return fieldMapping.getColumnName() + " " + ctx.getChild(1).getText() + " ?";
  }

  @Override
  public String visitWhereExpression(WhereStatementParser.WhereExpressionContext ctx) {
    return "WHERE " + visit(ctx.expression());
  }

  /**
   * Returns the JDBC parameter bindings generated by this visitor.
   *
   * @return An array of JdbcParameterBinding objects.
   */
  public JdbcParameterBinding<?>[] getJdbcParameterBindings() {
    return orderedJdbcParameters.toArray(new JdbcParameterBinding<?>[0]);
  }

  private String generatePlaceholders(Collection<?> collection) {
    return String.join(", ", Collections.nCopies(collection.size(), "?"));
  }

  /**
   * Validates that a parameter exists in the parameters map.
   *
   * @param parameterName The name of the parameter to validate.
   *
   * @throws BibernateBqlMissingParameterException If the parameter is not defined within parameters.
   */
  private void validateParameterExistence(String parameterName) {
    if (!parameters.containsKey(parameterName)) {
      throw new BibernateBqlMissingParameterException("Parameter with name: %s is not defined within parameters".formatted(parameterName));
    }
  }

  private FieldMapping<?> getFieldMapping(String fieldName) {
    return Optional.ofNullable(metamodel.getEntityMappingMap()
                                 .get(entityClass)
                                 .getFieldNameMappingMap()
                                 .get(fieldName))
      .orElseThrow(() -> new BibernateBqlInvalidParameterException("Required parameter: %s is not defined within %s".formatted(fieldName, entityClass)));
  }

}
