package com.bibernate.hoverla.query;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.tree.ParseTree;

import com.bibernate.hoverla.jdbc.JdbcResultExtractor;
import com.bibernate.hoverla.jdbc.types.BibernateJdbcType;
import com.bibernate.hoverla.metamodel.EntityMapping;
import com.bibernate.hoverla.metamodel.FieldMapping;
import com.bibernate.hoverla.session.SessionImplementor;
import com.bibernate.hoverla.session.cache.EntityKey;
import com.bibernate.hoverla.utils.EntityUtils;

import lombok.extern.slf4j.Slf4j;

import static com.bibernate.hoverla.utils.EntityUtils.getEntityKeyFromEntity;
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
 * <p>Note that the query language is case-sensitive.</p>
 * @see
 *
 * @param <T> The type of entities or objects that the query will return.
 */
@Slf4j
public class QueryImpl<T> implements Query<T> {

  private final static String SELECT_TEMPLATE = "SELECT %s FROM %s %s";
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

  public QueryImpl<T> setParameter(String paramName, Object paramValue) {
    parameters.put(paramName, paramValue);
    return this;
  }

  public List<T> getResult() {
    log.debug("Executing query with expression: {}", queryExpression);
    SqlJdbcStatement<T> sqlStatement = generateSqlJdbcStatement();

    List<Object[]> result = session.getJdbcExecutor()
      .executeSelectQuery(sqlStatement.getSqlTemplate(),
                          sqlStatement.getGetOrderedParameters(),
                          sqlStatement.getJdbcResultExtractors());

    log.debug("Query executed successfully. Mapping results to entities.");
    List<T> entities = result.stream()
      .map(this::map)
      .collect(Collectors.toList());

    log.debug("Mapping completed. Returning {} entities.", entities.size());

    return entities;
  }

  public SqlJdbcStatement<T> generateSqlJdbcStatement() {
    log.debug("Generating SQL statement for query: {}, entityClass: {}", queryExpression, resultType);
    var entityMapping = getEntityMapping();
    var fieldMappings = entityMapping.getFieldMappingMap().values();

    var columnNames = getColumnNames(fieldMappings);
    var jdbcResultExtractors = getJdbcTypes(fieldMappings);

    ParseTree abstractSyntaxTree = parseWhereStatement(queryExpression);
    BibernateWhereStatementVisitor visitor = new BibernateWhereStatementVisitor(session.getSessionFactory().getMetamodel(), resultType, parameters);
    String sqlWhereStatement = visitor.visit(abstractSyntaxTree);

    String sqlTemplate = SELECT_TEMPLATE.formatted(columnNames, entityMapping.getTableName(), sqlWhereStatement);

    log.debug("SQL statement generated: {}", sqlTemplate);

    return new SqlJdbcStatement<>(sqlTemplate,
                                  visitor.getJdbcParameterBindings(),
                                  jdbcResultExtractors.toArray(new JdbcResultExtractor<?>[0]));
  }

  private List<? extends BibernateJdbcType<?>> getJdbcTypes(Collection<FieldMapping<?>> fieldMappings) {
    return fieldMappings.stream()
      .map(FieldMapping::getJdbcType)
      .toList();
  }

  private String getColumnNames(Collection<FieldMapping<?>> fieldMappings) {
    return fieldMappings.stream()
      .map(FieldMapping::getColumnName)
      .collect(Collectors.joining(", "));
  }

  @SuppressWarnings("unchecked")
  private T map(Object[] row) {
    EntityMapping entityMapping = getEntityMapping();
    T entity = createEntityFromRow(row, entityMapping);
    EntityKey entityKey = getEntityKeyFromEntity(resultType, entity, session.getSessionFactory().getMetamodel());

    return (T) session
      .getPersistenceContext()
      .putEntityIfAbsent(entityKey, entity);
  }

  private EntityMapping getEntityMapping() {
    return session.getSessionFactory()
      .getMetamodel()
      .getEntityMappingMap()
      .get(resultType);
  }

  private T createEntityFromRow(Object[] row, EntityMapping entityMapping) {
    T entity = EntityUtils.newInstanceOf(resultType);
    int i = 0;
    for (var value : entityMapping.getFieldMappingMap().values()) {
      EntityUtils.setFieldValue(value.getFieldName(), entity, row[i++]);
    }
    return entity;
  }

}