package com.bibernate.hoverla.session;

import javax.sql.DataSource;

import com.bibernate.hoverla.metamodel.Metamodel;

/**
 * An internal contract that extends the SessionFactory interface for internal framework usage.
 *
 * <p>This interface extends the SessionFactory interface to provide additional methods for retrieving
 * the metamodel and data source. It is intended for internal use within the framework and should not
 * be used directly by external developers.</p>
 */
public interface SessionFactoryImplementor extends SessionFactory {

  /**
   * Retrieves the metamodel associated with this session factory.
   *
   * @return The metamodel providing information about entity classes and their associations.
   */
  Metamodel getMetamodel();

  /**
   * Retrieves the data source associated with this session factory.
   *
   * @return The data source used for database connections within the framework.
   */
  DataSource getDataSource();

}
