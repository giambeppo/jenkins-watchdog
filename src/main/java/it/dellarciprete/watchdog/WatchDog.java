package it.dellarciprete.watchdog;

import java.util.logging.Level;
import java.util.logging.Logger;

import it.dellarciprete.watchdog.utils.WatchDogException;
import it.dellarciprete.watchdog.utils.WatchDogLogger;

/**
 * Checks for some condition and, if needed, sends an alert.
 */
public abstract class WatchDog<T> {

  private static final Logger LOGGER = WatchDogLogger.get();

  private String name;
  private Watcher<T> watcher;
  private LatestFailure<T> latestFailure;
  private Alerter alerter;

  public WatchDog(Watcher<T> watcher, LatestFailure<T> latestFailure, Alerter alerter) {
    this.watcher = watcher;
    this.latestFailure = latestFailure;
    this.alerter = alerter;
  }

  /**
   * Tries to retrieve the latest status, checks if it is a new failure, and if so it tries to send an alert.
   * 
   * <p>If the alert cannot be sent, it tries to send it again in a later iteration, unless the status has been restored
   * to successful.</p>
   * 
   * @throws WatchDogException
   */
  public void watch() throws WatchDogException {
    Status<T> buildStatus;
    try {
      buildStatus = watcher.getLatestStatus();
    } catch (WatchDogException e) {
      alerter.sendAlertIfPending();
      throw e;
    }
    if (buildStatus.isFailure()) {
      LOGGER.log(Level.WARNING, "Build failed, checking if it's a new one");
      T latestFailureId = latestFailure.getId();
      if (latestFailureId == null || !latestFailureId.equals(buildStatus.getId())) {
        LOGGER.log(Level.WARNING, "The failed build is new, must send an alert");
        latestFailure.setId(buildStatus.getId());
        alerter.sendAlert(buildStatus.getParams());
      } else {
        alerter.sendAlertIfPending();
      }
    } else {
      alerter.resetPendingAlert();
    }
	}

  /**
   * Identifies this watchdog.
   * 
   * @return the name of the watchdog
   */
  public String getName() {
    return name;
  }

}
