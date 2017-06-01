package it.dellarciprete.watchdog;

import it.dellarciprete.watchdog.utils.WatchDogException;

/**
 * Keeps track of the id of the latest failure detected.
 * 
 * <p>This allows the watchdogs to make sure that a detected failure is in fact a new failure, in order not to send
 * alerts more than once.</p>
 *
 * @param <T> the type used for identifying a failure
 */
public interface LatestFailure<T> {

  /**
   * Retrieves the id of the latest build failure detected.
   * 
   * @return the id of the latest build failure, null if no failure has been detected yet
   * @throws WatchDogException
   */
  public T getId() throws WatchDogException;

  /**
   * Sets the id of the latest build failure detected.
   * 
   * @param id the id of the latest build failure
   * @throws WatchDogException
   */
  public void setId(T id) throws WatchDogException;

}
