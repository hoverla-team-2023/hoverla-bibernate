# Hoverla Bibernate

Bibernate is an advanced ORM (Object-Relational Mapping) framework designed to facilitate seamless data interaction between Java applications and relational databases. This document provides detailed guidance on configuring Bibernate, a lightweight and high-performance JDBC connection pool.

## Configuration

Bibernate leverages connection pool for its datasource management, ensuring optimal database performance and resource utilization. Below is the recommended configuration setup for integrating HikariCP with Bibernate:

```yaml
bibernate:
  connection-pool:
    type: hikari
  dataSourceClassName: com.zaxxer.hikari.HikariDataSource
  dataSource:
    jdbcUrl: jdbc:postgresql://host:5432/db
    username: user
    password: pass
    minimumIdle: 5
    idleTimeout: 600000
    maximumPoolSize: 15
    autoCommit: true
    poolName: HikariCorePool
    maxLifetime: 1800000
    connectionTimeout: 30000
```

## Getting Started

To get started with Bibernate:

1. **Configure DataSource**: Set up your datasource in an `config.yml`, `config.xml` or `config.properties` file as described in the configuration section. Ensure to replace the placeholders with your actual database details.

2. **Initialize Bibernate**: Utilize Bibernate's session factory to manage database sessions for performing CRUD operations.

## Example Usage

```java
public class Example {

    public void setup() {
      // Load properties
      CommonConfig commonConfig = CommonConfig.of("config.yml");
      // Add more configurations as needed

        Configuration configuration = Configuration.builder()
                .packageName(this.getClass().getPackageName()) // Scan metamodel
                .properties(commonConfig) // Add properties
                .annotatedClasses() // Add your entity classes here
                .build();

        // Now you can use configuration.getSessionFactory() to get the session factory
        // and manage your database sessions.
    }
}
```

Features:

- **Metamodel**:  Stands as the ultimate solution for Java entity classes`@Entity`, eliminating the need for runtime reflection while providing a centralized repository for metadata, including mappings and strategies.
- **Early Validation**: Empowerment Through Metamodel The Metamodel feature offers early validation capabilities for Java entity classes, providing developers with a streamlined approach to ensuring correctness and accuracy from the outset of the development process.
- **Fine-tuned Field Configuration**: Tailor field behavior in database interactions with attributes like insertable, updatable, and transient, offering granular control over column properties.
- **Support for `*.yml`, `*.properties`, `*.xml`** configuration files: Compatibility with various configuration file formats for easier setup.
- **Hikari Connection Pool Advanced Configuration**: Harness the full potential of the Hikari DataSource by fine-tuning advanced options for optimal performance.
- **Manual Id Assignment**: Ability to manually assign identifiers to entities.
- **Sequence Generator**: Automate identifier generation by utilizing database sequences, ensuring efficient and reliable entity identification.
- **Identity Generator**: Automatic generation of unique identifiers for entities.
- **CRUD Operations**: Basic Create, Read, Update, and Delete operations for entities.
- **Dirty Check**: Perform dirty checking to determine if entity fields have been modified before updating the database, ensuring only relevant changes are persisted.- 
- **Transaction Management**: Support for managing transactions, including begin, commit, and rollback.
- **Dynamic Update**: Support dynamic updating of entities, where only modified fields are included in update statements.
- **Write Behind Cache**: Caching mechanism that writes data to the database asynchronously.
- **First Level Cache**: Cache mechanism for storing recently accessed entities in memory.
- **Lazy Loading with Proxy for `@ManyToOne` Relationships**: Lazy loading of one-to-many relationships using proxy objects.
- **Optimistic Locking**: Entities can be marked with an optimistic lock, typically represented by a version counter field, to manage concurrent updates. This mechanism allows the database to detect conflicts when multiple users try to modify the same entity simultaneously.
- **Pessimistic Locking**: Concurrency control mechanism to lock entities to prevent other transactions from accessing them.
- **Lazy Loading for Collections `@OneToMany`**: Loading of collection attributes only when accessed, for performance optimization.
- **Advanced Custom JDBC Types**: Support for custom JDBC types for more complex data mapping scenarios e.g. `@JdbcType(PostgreSqlJdbcEnumType.class)`
- **BQL (Bibernate Query Language)**: BQL simplifies query construction by providing a robust grammar-based query language, enhancing the querying experience.- **Optimistic Locks**: Concurrency control mechanism to prevent concurrent updates to the same entity.
