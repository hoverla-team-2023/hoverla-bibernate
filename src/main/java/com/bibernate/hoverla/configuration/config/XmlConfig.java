package com.bibernate.hoverla.configuration.config;

import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import com.bibernate.hoverla.exceptions.ConfigurationException;
import com.bibernate.hoverla.utils.ConfigUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public class XmlConfig implements CommonConfig {

  private final Properties properties;

  public XmlConfig(String name) {
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
    var mapper = new XmlMapper();
    try (InputStream input = XmlConfig.class.getClassLoader().getResourceAsStream(name)) {
      if (input != null) {
        return ConfigUtils.toProperties(mapper.readValue(input, new TypeReference<>() {}));
      }
      throw new ConfigurationException("XML file not found in the classpath");
    } catch (Exception e) {
      throw new ConfigurationException("Error loading XML file", e);
    }
  }
}
