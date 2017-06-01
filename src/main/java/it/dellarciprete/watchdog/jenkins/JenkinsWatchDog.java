package it.dellarciprete.watchdog.jenkins;

import it.dellarciprete.watchdog.WatchDog;
import it.dellarciprete.watchdog.common.FileSystemLatestFailure;
import it.dellarciprete.watchdog.common.MailClient;
import it.dellarciprete.watchdog.utils.Configuration;
import it.dellarciprete.watchdog.utils.WatchDogException;

/**
 * WtachDog for checking build failures in a Jenkins job.
 */
public class JenkinsWatchDog extends WatchDog<Integer> {

  public JenkinsWatchDog(Configuration config) throws WatchDogException {
    super(new JenkinsClient(config),
        new FileSystemLatestFailure<Integer>(config.get("jenkins.file.latest.failure", "jenkinsLatestFailure.ser")),
        new MailClient(config, config.get("jenkins.mail.file.pending", "jenkinsPendingEmail.ser"),
            config.get("jenkins.mail.subject", "Jenkins build failed"),
            config.get("jenkins.mail.body", "A Jenkins build (%s) has failed, please take action!")));
  }

}
