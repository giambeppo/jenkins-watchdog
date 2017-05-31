package it.dellarciprete.watchdog.jenkins;

public class Main {

  public static void main(String[] args) throws InterruptedException, WatchDogException {
    WatchDogTrayIcon.initializeTrayIcon();
    String propertiesFile = System.getProperty("propertiesFile", "config.properties");
    Configuration config = new Configuration(propertiesFile);
    JenkinsClient jenkinsClient = new JenkinsClient(config);
    LatestFailure latestFailure = new LatestFailure(config);
    MailClient mailClient = new MailClient(config);
    new WatchDog(config, jenkinsClient, latestFailure, mailClient).startControlLoop();
  }

}
