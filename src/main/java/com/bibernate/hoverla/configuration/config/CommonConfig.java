package com.bibernate.hoverla.configuration.config;

import com.bibernate.hoverla.exceptions.ConfigurationException;

/**
 * The CommonConfig interface defines the methods for accessing
 * configuration details commonly required in database operations.
 * This includes methods to get JDBC URL and generic properties.
 */
public interface CommonConfig {

  String XML_PATTERN = "xml";
  String YML_PATTERN = "yml";
  String PROPERTIES_PATTERN = "properties";

  /**
   * Retrieves the value of a specified property.
   *
   * @param key The key of the property to retrieve.
   *
   * @return The value of the specified property as a String.
   */
  String getProperty(String key);

  /**
   * Sets the value of a specified property.
   *
   * @param key   The key of the property to set.
   * @param value The value to set for the specified property.
   */
  void setProperty(String key, String value);

  /**
   * Loads configuration from the specified path based on the file extension.
   * This method dynamically determines the type of configuration file
   * (XML, YML, or properties) and returns an appropriate implementation
   * of the CommonConfig interface that can handle that file type.
   *
   * @param configPath The path of the configuration file to load.
   *
   * @return An instance of CommonConfig suitable for the file type, or null if the file type is unsupported.
   */
  static CommonConfig of(String configPath) {
    if (configPath == null) {
      throw new IllegalStateException("File not found in the classpath");
    }

    // Determine the file extension to decide which implementation to use
    String fileExtension = configPath.substring(configPath.lastIndexOf(".") + 1);

    return switch (fileExtension) {
      case XML_PATTERN -> new XmlConfig(configPath);
      case YML_PATTERN -> new YmlConfig(configPath);
      case PROPERTIES_PATTERN -> new PropertiesConfig(configPath);
      default -> throw new ConfigurationException("Config file unsupported with extensions");
    };
  }
}
