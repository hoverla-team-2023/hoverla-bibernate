package com.bibernate.hoverla.jdbc;

import lombok.Data;

@Data
public class JdbcParameterBinding {

  private Object bindValue;
  private JdbcParameterBinder<?> bindType;

}
