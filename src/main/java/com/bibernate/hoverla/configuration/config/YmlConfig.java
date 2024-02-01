package com.bibernate.hoverla.configuration.config;

import java.io.InputStream;
import java.util.Properties;

import com.bibernate.hoverla.exceptions.ConfigurationException;
import com.bibernate.hoverla.utils.ConfigUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class YmlConfig implements CommonConfig {

  /**
   * The YmlConfig class is an implementation of the CommonConfig interface that reads configuration
   * properties from a YML file. It provides methods to retrieve and modify the properties.
   */
  private final Properties properties;

  /**
   * The YmlConfig class is an implementation of the CommonConfig interface that reads configuration
   * properties from a YAML file. It provides methods to retrieve and modify the properties.
   */
  public YmlConfig(String name) {
    properties = readProperties(name);
  }

  /**
   * Retrieves the value of a specified property.
   *
   * @param key The key of the property to retrieve.
   * @return The value of the specified property as a String.
   */
  @Override
  public String getProperty(String key) {
    return properties.getProperty(key);
  }

  /**
   * Sets the value of a specified property.
   *
   * @param key   The key of the property to set.
   * @param value The value to set for the specified property.
   */
  @Override
  public void setProperty(String key, String value) {
     properties.put(key, value);
  }


  /**
   * Reads properties from a YAML file and returns them as a Properties object.
   *
   * @param name The name of the YAML file to be read.
   * @return A Properties object containing the properties from the YAML file.
   * @throws ConfigurationException If the YAML file is not found or there is an error loading the file.
   */
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
