package com.bibernate.hoverla.configuration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.bibernate.hoverla.configuration.config.CommonConfig;
import com.bibernate.hoverla.configuration.config.PropertiesConfig;
import com.bibernate.hoverla.configuration.config.XmlConfig;
import com.bibernate.hoverla.configuration.config.YmlConfig;
import com.bibernate.hoverla.exceptions.ConfigurationException;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class CommonConfigTest {

  @Test
  public void testOfXml() {
    CommonConfig config = CommonConfig.of("config/test-config.xml");
    assertTrue(config instanceof XmlConfig);
  }

  @Test
  public void testOfYml() {
    CommonConfig config = CommonConfig.of("config/test-config.yml");
    assertTrue(config instanceof YmlConfig);
  }

  @Test
  public void testOfProperties() {
    CommonConfig config = CommonConfig.of("config/test-config.properties");
    assertTrue(config instanceof PropertiesConfig);
  }

  @Test
  public void testOfUnsupportedExtension() {
    Assertions.assertThrows(ConfigurationException.class, () -> CommonConfig.of("unsupported.txt"));
  }

}
