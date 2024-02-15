package com.bibernate.hoverla.query;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.bibernate.hoverla.jdbc.JdbcResultExtractor;
import com.bibernate.hoverla.metamodel.EntityMapping;
import com.bibernate.hoverla.session.Session;
import com.bibernate.hoverla.session.SessionImplementor;
import com.bibernate.hoverla.session.cache.EntityEntry;
import com.bibernate.hoverla.session.cache.EntityKey;
import com.bibernate.hoverla.utils.EntityUtils;

import lombok.extern.slf4j.Slf4j;

import static com.bibernate.hoverla.utils.EntityUtils.getEntityKey;
import static com.bibernate.hoverla.utils.EntityUtils.parseWhereStatement;

/**
 * An implementation of the Query interface for executing custom queries using an object-oriented query language.
 *
 * <p>This class enables the construction and execution of custom queries using a specialized object-oriented query language.
 * The language supports parameterization, allowing to define and use parameters in queries for dynamic values. Additionally,
 * it provides support for logical operators (AND, OR), and comparison operators (<, >, <=, >=, =) for filtering and retrieving data
 * from a database or data source. It also offers grouping with parentheses and membership checks (IN).</p>
 *
 * <p>The object-oriented query language offers flexibility for creating queries tailored to specific
 * application requirements, making it ideal for scenarios where a domain-specific query syntax is preferred.</p>
 *
 * <p>An example:</p>
 *
 * <pre>{@code
 * List<MyEntity> result = new QueryImpl<>(session,
 * "WHERE age > :ageParam AND name = :nameParam", MyEntity.class)
 *      .setParameter("ageParam", 30)
 *      .setParameter("nameParam", "John");
 *
 * }</pre>
 *
 * see: {@link Session#createQuery(String, Class)}
 * <p>Note that the query language is case-sensitive.</p>
 *
 * @param <T> The type of entities or objects that the query will return.
 */
@Slf4j
public class QueryImpl<T> implements Query<T> {

  private final static String SELECT_TEMPLATE = "SELECT %s FROM %s %s;";
  private final String queryExpression;
  private final Map<String, Object> parameters;
  private final Class<T> resultType;
  private final SessionImplementor session;

  public QueryImpl(SessionImplementor session, String criteria, Class<T> entityClass) {
    this.queryExpression = criteria;
    this.parameters = new HashMap<>();
    this.resultType = entityClass;
    this.session = session;
  }

  /**
   * Sets a parameter for the query.
   *
   * @param paramName  The name of the parameter.
   * @param paramValue The value of the parameter.
   *
   * @return The QueryImpl instance.
   */
  public QueryImpl<T> setParameter(String paramName, Object paramValue) {
    parameters.put(paramName, paramValue);
    return this;
  }

  /**
   * Executes the query and returns the result as a list of entities.
   *
   * @return The list of entities resulting from the query.
   */
  public List<T> getResult() {
    log.debug("Executing query with expression: {}", queryExpression);
    SqlJdbcStatement sqlStatement = generateSqlJdbcStatement();

    List<Object[]> result = session.getJdbcExecutor()
      .executeSelectQuery(sqlStatement.getSqlTemplate(),
                          sqlStatement.getGetOrderedParameters(),
                          sqlStatement.getJdbcResultExtractors());

    log.debug("Query executed successfully. Mapping results to entities.");
    List<T> entities = result.stream()
      .map(this::mapRowToEntity)
      .collect(Collectors.toList());

    log.debug("Mapping completed. Returning {} entities.", entities.size());

    return entities;
  }

  public SqlJdbcStatement generateSqlJdbcStatement() {
    log.debug("Generating SQL statement for query: {}, entityClass: {}", queryExpression, resultType);
    var entityMapping = session.getEntityMapping(resultType);

    var columnNames = entityMapping.getColumnNames();
    var jdbcResultExtractors = entityMapping.getJdbcTypes();
    var tableName = entityMapping.getTableName();

    var abstractSyntaxTree = parseWhereStatement(queryExpression);
    var visitor = new BibernateWhereStatementVisitor(session.getSessionFactory().getMetamodel(), resultType, parameters);
    var sqlWhereStatement = visitor.visit(abstractSyntaxTree);

    var sqlTemplate = SELECT_TEMPLATE.formatted(columnNames, tableName, sqlWhereStatement);

    log.debug("SQL statement generated: {}", sqlTemplate);

    return new SqlJdbcStatement(sqlTemplate,
                                visitor.getJdbcParameterBindings(),
                                jdbcResultExtractors.toArray(new JdbcResultExtractor<?>[0]));
  }

  /**
   * Maps a row of query results to an entity.
   *
   * @param row The row of query results.
   *
   * @return The entity created from the row of results.
   */
  private T mapRowToEntity(Object[] row) {
    EntityMapping entityMapping = getEntityMapping();
    T entity = session.getEntityRowMapper().createEntityFromRow(row, resultType);
    EntityKey<T> entityKey = getEntityKey(resultType, entity, entityMapping.getPrimaryKeyMapping().getFieldName());

    return Optional.ofNullable((session.getPersistenceContext().manageEntity(entityKey, () -> entity, entityEntry -> {})))
      .map(EntityEntry::getEntity)
      .map(resultType::cast)
      .orElse(null);
  }

  private EntityMapping getEntityMapping() {
    return session.getEntityMapping(resultType);
  }

}