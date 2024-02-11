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

import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;

import static com.bibernate.hoverla.utils.ConfigUtils.replace;

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

  @Override
  public Map<String, String> getAllProperties(String prefix) {
    return properties.entrySet().stream()
      .filter(entry -> containsIgnoreCase(entry.getKey().toString(), prefix))
      .map(e -> Pair.of(replace(e.getKey().toString(), prefix), e.getValue().toString()))
      .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
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
