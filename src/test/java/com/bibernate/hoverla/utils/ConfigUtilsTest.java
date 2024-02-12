package com.bibernate.hoverla.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConfigUtilsTest {

  @Test
  public void replaceTest() {
    var porps = "bibernate.dataSourceClassName";
    var prefix = "dataSource";
    assertEquals("dataSourceClassName", ConfigUtils.replace(porps, prefix));
  }
}
