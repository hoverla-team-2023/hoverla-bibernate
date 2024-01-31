package com.bibernate.hoverla.configuration.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class YmlConfigTest {

  private CommonConfig config;

  @BeforeEach
  public void setUp() {
    config = CommonConfig.of("config/test-config.yml");
  }

  @Test
  public void shouldGetUrlProperty() {
    String url = config.getProperty("bibernate.connection.url");
    assertEquals("jdbc:mysql://localhost:3306/mydb", url);
  }

  @Test
  public void shouldSetUsernameProperty() {
    var username = "bibernate.connection.username";
    config.setProperty(username, "newUser");
    assertEquals("newUser", config.getProperty(username));
  }
}
