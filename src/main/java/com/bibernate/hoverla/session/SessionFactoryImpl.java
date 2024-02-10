package com.bibernate.hoverla.session;

import java.sql.Connection;

import javax.sql.DataSource;

import com.bibernate.hoverla.metamodel.Metamodel;

import lombok.Getter;

@Getter
public class SessionFactoryImpl implements SessionFactory, SessionFactoryImplementor {

  //todo remove connection from here use dataSource
  private final Connection connection;
  private DataSource dataSource;
  private Metamodel metamodel;

  public SessionFactoryImpl(Connection connection) {
    this.connection = connection;
  }

  public SessionFactoryImpl(DataSource dataSource, Metamodel metamodel) {
    this.dataSource = dataSource;
    this.metamodel = metamodel;
    this.connection = null;
  }

  @Override
  public Session openSession() {
    SessionImpl session = new SessionImpl(this);
    return session;
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