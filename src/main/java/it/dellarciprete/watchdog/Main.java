package it.dellarciprete.watchdog;

import java.util.logging.Level;

import it.dellarciprete.watchdog.jenkins.JenkinsWatchDog;
import it.dellarciprete.watchdog.utils.Configuration;
import it.dellarciprete.watchdog.utils.WatchDogException;
import it.dellarciprete.watchdog.utils.WatchDogLogger;
import it.dellarciprete.watchdog.utils.WatchDogTrayIcon;

public class Main {

  public static void main(String[] args) {
    try {
      WatchDogTrayIcon.initializeTrayIcon();
      String propertiesFile = System.getProperty("propertiesFile", "config.properties");
      Configuration config = new Configuration(propertiesFile);
      new WatchDogPen(config, new JenkinsWatchDog(config)).startControlLoop();
    } catch (WatchDogException e) {
      WatchDogLogger.get().log(Level.SEVERE, "Initialization error", e);
      WatchDogLogger.finalizeLogger();
    }
  }

}
