package it.dellarciprete.watchdog.sonarqube;

import it.dellarciprete.watchdog.WatchDog;
import it.dellarciprete.watchdog.common.FileSystemLatestFailure;
import it.dellarciprete.watchdog.common.MailClient;
import it.dellarciprete.watchdog.utils.Configuration;
import it.dellarciprete.watchdog.utils.WatchDogException;

public class SonarQubeWatchDog extends WatchDog<String> {

  public SonarQubeWatchDog(Configuration config) throws WatchDogException {
    super(new SonarQubeClient(config),
        new FileSystemLatestFailure<String>(config.get("sonarqube.file.latest.failure", "sonarLatestFailure.ser")),
        new MailClient(config, config.get("sonarqube.mail.file.pending", "sonarPendingEmail.ser"),
            config.get("sonarqube.mail.subject", "SonarQube quality gate failed"), config.get("sonarqube.mail.body",
                "A SonarQube project (%s) did not pass the quality gate measures!")));
  }

}
