package com.bibernate.hoverla.configuration.config;

import java.io.InputStream;
import java.util.Properties;

import com.bibernate.hoverla.exceptions.ConfigurationException;
import com.bibernate.hoverla.utils.ConfigUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class YmlConfig implements CommonConfig {

  private final Properties properties;

  public YmlConfig(String name) {
    properties = readProperties(name);
  }

  @Override
  public String getProperty(String key) {
    return properties.getProperty(key);
  }

  @Override
  public void setProperty(String key, String value) {
     properties.put(key, value);
  }


  private static Properties readProperties(String name) {
    var mapper = new ObjectMapper(new YAMLFactory());
    try (InputStream input = YmlConfig.class.getClassLoader().getResourceAsStream(name)) {
      if (input != null) {
        return ConfigUtils.toProperties(mapper.readValue(input, new TypeReference<>() {}));
      }
      throw new ConfigurationException("Yaml file not found in the classpath");
    } catch (Exception e) {
      throw new ConfigurationException("Error loading yaml file", e);
    }
  }


}
