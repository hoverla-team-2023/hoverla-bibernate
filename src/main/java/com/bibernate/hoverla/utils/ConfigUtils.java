package com.bibernate.hoverla.utils;

import java.util.Map;
import java.util.Properties;

public class ConfigUtils {

  /**
   * Converts a nested map structure into a Properties object. Nested keys are concatenated with dots.
   *
   * @param map The nested map to be converted to Properties.
   * @return Properties object containing all keys and values from the map, with nested keys concatenated.
   */
  public static Properties toProperties(Map<String, Object> map){
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
