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
  /**
   * This static method flattens a nested map structure into a Properties object.
   * It recursively traverses the map, concatenating keys with a dot ('.') to create a path.
   * If a value is another map, it is treated as a nested structure and the method is called recursively.
   * If a value is not a map, it is converted to a string and set as a property in the provided Properties object.
   *
   * @param path The current path or key in the nested map structure.
   * @param map The map to be flattened.
   * @param properties The Properties object where the flattened key-value pairs will be stored.
   */
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
