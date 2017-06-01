package it.dellarciprete.watchdog;

import it.dellarciprete.watchdog.utils.WatchDogException;

/**
 * Handles sending the alert in case of a new build failure.
 */
public interface Alerter {

  /**
   * Tries to send an alert.
   * 
   * <p>If it fails, it should make sure that the alert can be re-sent later through {@link #sendAlertIfPending()}.</p>
   * 
   * @param params the parameters used to customize the alert message
   * @throws WatchDogException
   */
  public void sendAlert(Object... params) throws WatchDogException;

  /**
   * If there is an alert pending, tries to send it again. Otherwise, nothing happens.
   * 
   * @throws WatchDogException
   */
  public void sendAlertIfPending() throws WatchDogException;

  /**
   * If there is an alert pending, deletes it.
   * 
   * @throws WatchDogException
   */
  public void resetPendingAlert() throws WatchDogException;

}
