package it.dellarciprete.watchdog;

import it.dellarciprete.watchdog.utils.WatchDogException;

/**
 * Handles the retrieval of the latest status for the watched phenomenon.
 *
 * @param <T> the type used for identifying an instance of the phenomenon
 */
public interface Watcher<T> {

  /**
   * Tries to retrieve the latest status of the watched phenomenon.
   * 
   * @return the latest status
   * @throws WatchDogException
   */
  public Status<T> getLatestStatus() throws WatchDogException;

}
