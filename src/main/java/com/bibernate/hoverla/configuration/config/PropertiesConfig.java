package com.bibernate.hoverla.configuration.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import com.bibernate.hoverla.exceptions.ConfigurationException;

import lombok.extern.slf4j.Slf4j;

import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;

import static com.bibernate.hoverla.utils.ConfigUtils.replace;

@Slf4j
public class PropertiesConfig implements CommonConfig {

  private final Properties properties;

  /**
   * Constructs a new PropertiesConfig instance by loading properties from the specified file.
   *
   * @param name The name of the properties file to load.
   *
   * @throws ConfigurationException If the properties file cannot be loaded.
   */
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

  /**
   * Retrieves all properties that start with the specified prefix.
   * The returned map contains the properties with the prefix removed from the keys.
   *
   * @param prefix The prefix to filter the properties by.
   *
   * @return A map of properties with the prefix removed from the keys.
   */
  @Override
  public Map<String, String> getAllProperties(String prefix) {
    return properties.entrySet().stream()
      .filter(entry -> containsIgnoreCase(entry.getKey().toString(), prefix))
      .map(e -> Pair.of(replace(e.getKey().toString(), prefix), e.getValue().toString()))
      .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
  }

  /**
   * Reads and loads properties from a file with the given name.
   *
   * @param name The name of the properties file.
   *
   * @return The loaded properties.
   *
   * @throws ConfigurationException If the file cannot be found or read.
   */
  private static Properties readProperties(String name) {
    var properties = new Properties();
    try (InputStream input = PropertiesConfig.class.getClassLoader().getResourceAsStream(name)) {
      if (input != null) {
        properties.load(input);
        return properties;
      }
      log.warn("Properties file not found in the classpath: " + name);
      throw new ConfigurationException("Properties file not found in the classpath: " + name);
    } catch (IOException e) {
      log.error("Error loading properties file: " + e.getMessage());
      throw new ConfigurationException("Error loading properties file", e);
    }
  }

}
