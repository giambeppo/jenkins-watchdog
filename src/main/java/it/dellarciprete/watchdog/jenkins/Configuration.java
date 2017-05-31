package it.dellarciprete.watchdog.jenkins;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Exposed the configuration loaded from a properties file.
 */
public class Configuration {

  private final Properties configuration = new Properties();

  public Configuration(String propertyFile) {
    try {
      FileInputStream in = new FileInputStream(propertyFile);
      configuration.load(in);
      in.close();
    } catch (IOException e) {
      throw new RuntimeException("Unable to load properties from " + propertyFile, e);
    }
  }

  public String get(String property) {
    return configuration.getProperty(property);
  }

  public String get(String property, String defaultValue) {
    return configuration.getProperty(property, defaultValue);
  }

}
