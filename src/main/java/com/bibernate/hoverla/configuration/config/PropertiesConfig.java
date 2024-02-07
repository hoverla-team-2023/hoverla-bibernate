package com.bibernate.hoverla.configuration.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import com.bibernate.hoverla.exceptions.ConfigurationException;

public class PropertiesConfig implements CommonConfig {

  private final Properties properties;

  public PropertiesConfig(String name) {
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

  @Override
  public Map<String, String> getAllProperties(String prefix) {
    return properties.entrySet().stream()
      .filter(entry -> entry.getKey().toString().contains(prefix))
      .collect(Collectors.toMap(
        e -> e.getKey().toString(),
        e -> e.getValue().toString()
      ));
  }

  private static Properties readProperties(String name) {
    var properties = new Properties();
    try (InputStream input = PropertiesConfig.class.getClassLoader().getResourceAsStream(name)) {
      if (input != null) {
        properties.load(input);
        return properties;
      }
      throw new ConfigurationException("Properties file not found in the classpath");
    } catch (IOException e) {
      throw new ConfigurationException("Error loading properties file", e);
    }
  }

}
