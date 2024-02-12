package com.bibernate.hoverla.configuration.config;

import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import com.bibernate.hoverla.exceptions.ConfigurationException;
import com.bibernate.hoverla.utils.ConfigUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import lombok.extern.slf4j.Slf4j;

import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;

@Slf4j
public class XmlConfig implements CommonConfig {

  private final Properties properties;

  /**
   * Constructs a new XmlConfig instance by loading properties from the specified XML file.
   *
   * @param name The name of the XML file to load.
   *
   * @throws ConfigurationException If the XML file cannot be loaded.
   */
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

  /**
   * Retrieves all properties that start with the specified prefix, adjusting key names
   * by removing the prefix and converting to lower case for consistency.
   *
   * @param prefix The prefix to filter the properties by.
   *
   * @return A map of properties with modified keys.
   */
  @Override
  public Map<String, String> getAllProperties(String prefix) {
    return properties.entrySet().stream()
      .filter(entry -> containsIgnoreCase(entry.getKey().toString(), prefix))
      .map(e -> Pair.of(ConfigUtils.replace(e.getKey().toString().toLowerCase(), prefix), e.getValue().toString().toLowerCase()))
      .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
  }

  /**
   * Reads and loads properties from an XML file with the given name.
   *
   * @param name The name of the XML file.
   *
   * @return The loaded properties.
   *
   * @throws ConfigurationException If the file cannot be found or read.
   */
  private static Properties readProperties(String name) {
    XmlMapper mapper = new XmlMapper();
    try (InputStream input = XmlConfig.class.getClassLoader().getResourceAsStream(name)) {
      if (input != null) {
        log.info("Loading properties from XML file: {}", name);
        return ConfigUtils.toProperties(mapper.readValue(input, new TypeReference<>() {}));
      }
      log.error("XML file not found in the classpath: {}", name);
      throw new ConfigurationException("XML file not found in the classpath: " + name);
    } catch (Exception e) {
      log.error("Error loading XML file: {}", name, e);
      throw new ConfigurationException("Error loading XML file: " + name, e);
    }
  }

}
