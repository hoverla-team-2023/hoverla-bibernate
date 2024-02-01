package com.bibernate.hoverla.configuration.config;

import java.io.InputStream;
import java.util.Properties;

import com.bibernate.hoverla.exceptions.ConfigurationException;
import com.bibernate.hoverla.utils.ConfigUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

/**
 * The XmlConfig class is an implementation of the CommonConfig interface that reads configuration
 * properties from an XML file. It provides methods to retrieve and modify the properties.
 */
public class XmlConfig implements CommonConfig {

  /**
   *
   */
  private final Properties properties;

  /**
   * The XmlConfig class is an implementation of the CommonConfig interface that reads configuration
   * properties from an XML file.
   */
  public XmlConfig(String name) {
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
   * Reads configuration properties from an XML file and returns them as a Properties object.
   *
   * @param name The name of the XML file to be read.
   * @return A Properties object containing the configuration properties from the XML file.
   * @throws ConfigurationException If the XML file is not found or there is an error loading the file.
   */
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
