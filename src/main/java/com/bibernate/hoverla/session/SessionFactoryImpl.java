package com.bibernate.hoverla.session;

import javax.sql.DataSource;

import com.bibernate.hoverla.metamodel.Metamodel;

import lombok.Getter;

/**
 * Implementation of the {@link SessionFactory} and {@link SessionFactoryImplementor} interfaces.
 */
@Getter
public class SessionFactoryImpl implements SessionFactory, SessionFactoryImplementor {

  private final DataSource dataSource;
  private final Metamodel metamodel;

  public SessionFactoryImpl(DataSource dataSource, Metamodel metamodel) {
    this.dataSource = dataSource;
    this.metamodel = metamodel;
  }

  @Override
  public Session openSession() {
    return new SessionImpl(this);
  }

  @Override
  public Metamodel getMetamodel() {
    return metamodel;
  }

  @Override
  public DataSource getDataSource() {
    return dataSource;
  }

}