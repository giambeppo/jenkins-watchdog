package it.dellarciprete.watchdog.jenkins;

import java.util.logging.Level;

public class Main {

  public static void main(String[] args) {
    try {
      WatchDogTrayIcon.initializeTrayIcon();
      String propertiesFile = System.getProperty("propertiesFile", "config.properties");
      Configuration config = new Configuration(propertiesFile);
      JenkinsClient jenkinsClient = new JenkinsClient(config);
      LatestFailure latestFailure = new LatestFailure(config);
      MailClient mailClient = new MailClient(config);
      new WatchDog(config, jenkinsClient, latestFailure, mailClient).startControlLoop();
    } catch (WatchDogException e) {
      WatchDogLogger.get().log(Level.SEVERE, "Initialization error", e);
      WatchDogLogger.finalizeLogger();
    }
  }

}
