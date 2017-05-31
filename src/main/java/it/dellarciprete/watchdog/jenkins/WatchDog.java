package it.dellarciprete.watchdog.jenkins;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.MessagingException;

/**
 * Handles the infinite control loop and orchestrates its steps.
 */
public class WatchDog {

  private static final Logger LOGGER = WatchDogLogger.get();
  private static volatile boolean shuttingDown = false;

  private Configuration config;
  private JenkinsClient jenkinsClient;
  private LatestFailure latestFailure;
  private MailClient mailClient;

  public WatchDog(Configuration config, JenkinsClient jenkinsClient, LatestFailure latestFailure,
      MailClient mailClient) {
    this.config = config;
    this.jenkinsClient = jenkinsClient;
    this.latestFailure = latestFailure;
    this.mailClient = mailClient;
  }

  /**
   * Runs the infinite control loop.
   *
   * <p>It stops when the suthdown hook is executed.</p>
   */
  public void startControlLoop() {
    int pollingInterval = Integer.parseInt(config.get("polling.interval.minutes", "15"));
    Runtime.getRuntime().addShutdownHook(new WatchDogShutdownHook(Thread.currentThread()));
    while (!shuttingDown) {
      LOGGER.log(Level.INFO, "Entering the control loop");
      try {
        doCheck();
        LOGGER.log(Level.INFO, "Iteration completed");
      } catch (IOException | WatchDogException | MessagingException e) {
        LOGGER.log(Level.SEVERE, "Error while performing the controls", e);
      }
      if (shuttingDown) {
        break;
      }
      try {
        Thread.sleep(pollingInterval * 1000 * 60);
      } catch (InterruptedException e) {
        break;
      }
    }
  }

  /**
   * Tries to retriev the Jenkins build status, checks if it is a new failure, and if so it tries to send an alert
   * email.
   * 
   * <p>If the email cannot be sent, tries to send it again in a later iteration, unless the build status has been
   * restored to successful.</p>
   * 
   * @throws IOException
   * @throws WatchDogException
   * @throws MessagingException
   */
  private void doCheck() throws IOException, WatchDogException, MessagingException {
    BuildStatus buildStatus;
    try {
      buildStatus = jenkinsClient.getLatestBuildStatus();
    } catch (Exception e) {
      mailClient.sendAlertIfPending();
      throw e;
    }
    if (!buildStatus.isSuccess()) {
      LOGGER.log(Level.WARNING, "Build failed, checking if it's a new one");
      Integer latestFailureId = latestFailure.getId();
      if (latestFailureId == null || latestFailureId != buildStatus.getNumber()) {
        LOGGER.log(Level.WARNING, "The failed build is new, must send an alert");
        latestFailure.setId(buildStatus.getNumber());
        mailClient.sendAlert(buildStatus.getUrl());
      } else {
        mailClient.sendAlertIfPending();
      }
    } else {
      mailClient.resetPendingAlert();
    }
	}

  /**
   * Shutdown hook allowing for the completion of the currently running control iteration and the correct finalization
   * of the logger upon receiving the shutdown signal.
   */
  private class WatchDogShutdownHook extends Thread {

    private final Thread mainThread;

    public WatchDogShutdownHook(Thread mainThread) {
      this.mainThread = mainThread;
    }

    @Override
    public void run() {
      LOGGER.log(Level.INFO, "Shutdown signal received, waiting for the current control iteration to end");
      shuttingDown = true;
      try {
        mainThread.interrupt();
        mainThread.join();
        WatchDogLogger.finalizeLogger();
      } catch (InterruptedException e) {
        LOGGER.log(Level.SEVERE, "Error waiting for shutdown", e);
      }
    }

  }

}
