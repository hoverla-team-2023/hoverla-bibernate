package com.bibernate.hoverla.configuration.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.bibernate.hoverla.exceptions.ConfigurationException;

/**
 * The PropertiesConfig class implements the CommonConfig interface and provides methods to access and manipulate
 * configuration details stored in properties files.
 */
public class PropertiesConfig implements CommonConfig {
  /**
   * The private final variable "properties" represents a collection of key-value pairs that store configuration details.
   * It is an instance of the Properties class, which is a subclass of Hashtable.
   * This variable is declared as "final", which means its value cannot be changed once initialized.
   * It is used within the PropertiesConfig class to store configuration details loaded from properties files.
   * The "properties" variable is initialized by calling the "readProperties" method, passing the name of the properties file as a parameter.
   * A new instance of the Properties class is created, and the properties file is loaded into it using the "load" method.
   * The loaded properties can be accessed and manipulated using the "getProperty" and "setProperty" methods defined in the CommonConfig interface.
   * The "getProperty" method retrieves the value of a specified property based on its key.
   * The "setProperty" method sets the value of a specified property based on its key.
   * If the properties file cannot be found or there is an error loading it, a ConfigurationException is thrown.
   *
   * @see CommonConfig
   * @see PropertiesConfig
   * @see CommonConfig#getProperty(String)
   * @see CommonConfig#setProperty(String, String)
   * @see ConfigurationException
   */
  private final Properties properties;

  /**
   * Constructs a new instance of the PropertiesConfig class with the specified name.
   * This constructor initializes the properties field by calling the readProperties method.
   *
   * @param name The name of the properties file to read.
   */
  public PropertiesConfig(String name) {
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
   * Reads properties from a specified properties file.
   *
   * @param name The name of the properties file to read.
   * @return The loaded Properties object.
   * @throws ConfigurationException if the properties file is not found or if there is an error loading it.
   */
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
