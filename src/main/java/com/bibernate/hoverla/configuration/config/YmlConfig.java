package com.bibernate.hoverla.configuration.config;

import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import com.bibernate.hoverla.exceptions.ConfigurationException;
import com.bibernate.hoverla.utils.ConfigUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import lombok.extern.slf4j.Slf4j;

import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;

import static com.bibernate.hoverla.utils.ConfigUtils.replace;

@Slf4j
public class YmlConfig implements CommonConfig {

  private final Properties properties;

  /**
   * Constructs a new YmlConfig instance by loading properties from the specified YAML file.
   *
   * @param name The name of the YAML file to load.
   * @throws ConfigurationException If the YAML file cannot be loaded.
   */
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

  /**
   * Retrieves all properties that start with the specified prefix. The keys of the returned
   * map are the original keys with the prefix removed.
   *
   * @param prefix The prefix used to filter the property keys.
   * @return A map of properties filtered by the specified prefix.
   */
  @Override
  public Map<String, String> getAllProperties(String prefix) {
    return properties.entrySet().stream()
      .filter(entry -> containsIgnoreCase(entry.getKey().toString(), prefix))
      .map(e -> Pair.of(replace(e.getKey().toString(), prefix), e.getValue().toString()))
      .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
  }

  /**
   * Reads and loads properties from a YAML file with the given name.
   *
   * @param name The name of the YAML file.
   * @return The loaded properties.
   * @throws ConfigurationException If the file cannot be found or read.
   */
  private static Properties readProperties(String name) {
    var mapper = new ObjectMapper(new YAMLFactory());
    try (InputStream input = YmlConfig.class.getClassLoader().getResourceAsStream(name)) {
      if (input != null) {
        log.info("Loading properties from YAML file: {}", name);
        return ConfigUtils.toProperties(mapper.readValue(input, new TypeReference<Map<String, Object>>() {}));
      }
      log.error("YAML file not found in the classpath: {}", name);
      throw new ConfigurationException("YAML file not found in the classpath: " + name);
    } catch (Exception e) {
      log.error("Error loading YAML file: {}", name, e);
      throw new ConfigurationException("Error loading YAML file: " + name, e);
    }
  }
}
