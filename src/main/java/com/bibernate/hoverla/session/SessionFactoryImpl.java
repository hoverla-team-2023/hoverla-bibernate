package com.bibernate.hoverla.session;

import java.sql.Connection;

import javax.sql.DataSource;

import com.bibernate.hoverla.metamodel.Metamodel;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SessionFactoryImpl implements SessionFactory, SessionFactoryImplementor {

  //todo remove connection from here use dataSource
  private final Connection connection;

  private DataSource dataSource;
  private Metamodel metamodel;

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