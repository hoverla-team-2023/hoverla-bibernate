package com.bibernate.hoverla.utils;

import java.util.Map;
import java.util.Properties;

/**
 * Utility class for handling configuration properties.
 */
public class ConfigUtils {

  /**
   * Replaces the prefix in the property string with an empty string.
   *
   * @param property the property string to process
   * @param prefix   the prefix to be replaced
   * @return the property string with the prefix replaced or the original property string if prefix is not found
   * @throws IllegalArgumentException if either property or prefix is null
   */
  public static String replace(String property, String prefix) {
    if (property == null || prefix == null) {
      throw new IllegalArgumentException("Property or prefix cannot be null.");
    }
    return property.contains(prefix) ? property.substring(property.indexOf(prefix)) : property;
  }

  private static String doReplace(String property, String prefix) {
    return property.contains(prefix) ? property.substring(property.indexOf(prefix)) : property;
  }

  /**
   * Converts a nested map structure into a Properties object. Nested keys are concatenated with dots.
   *
   * @param map The nested map to be converted to Properties.
   *
   * @return Properties object containing all keys and values from the map, with nested keys concatenated.
   */
  public static Properties toProperties(Map<String, Object> map) {
    var props = new Properties();
    flatMap("", map, props);
    return props;
  }

  private static void flatMap(String path, Map<String, Object> map, Properties properties) {
    for (Map.Entry<String, Object> entry : map.entrySet()) {
      String key = entry.getKey();
      Object value = entry.getValue();
      String newPath = path.isEmpty() ? key : path + "." + key;
      if (value instanceof Map) {
        flatMap(newPath, (Map<String, Object>) value, properties);
      } else {
        properties.setProperty(newPath, value.toString());
      }
    }
  }
}
