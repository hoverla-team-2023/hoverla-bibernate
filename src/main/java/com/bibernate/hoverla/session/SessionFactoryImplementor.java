package com.bibernate.hoverla.session;

import javax.sql.DataSource;

import com.bibernate.hoverla.metamodel.Metamodel;

public interface SessionFactoryImplementor extends SessionFactory {

  Metamodel getMetamodel();

  DataSource getDataSource();

}
