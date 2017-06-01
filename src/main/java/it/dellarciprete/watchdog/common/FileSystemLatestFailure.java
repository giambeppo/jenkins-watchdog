package it.dellarciprete.watchdog.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import it.dellarciprete.watchdog.LatestFailure;
import it.dellarciprete.watchdog.utils.WatchDogException;
import it.dellarciprete.watchdog.utils.WatchDogLogger;

/**
 * Stores the latest failure id on the filesystem.
 */
public class FileSystemLatestFailure<T> implements LatestFailure<T> {

  private static final Logger LOGGER = WatchDogLogger.get();

  private final File file;

  public FileSystemLatestFailure(String fileName) {
    file = new File(fileName);
  }

  /**
   * Retrieves the id of the latest failure detected.
   * 
   * @return the id of the latest failure, null if no failure has been detected yet
   * @throws WatchDogException
   */
  @SuppressWarnings("unchecked")
  @Override
  public T getId() throws WatchDogException {
    LOGGER.log(Level.INFO, "Retrieving latest detected failure from " + file.getName());
    T latestFailure = null;
    if (file.exists()) {
      try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
        latestFailure = (T) ois.readObject();
      } catch (IOException | ClassNotFoundException e) {
        throw new WatchDogException(e);
      }
    }
    return latestFailure;
  }

  /**
   * Set the id of the latest failure detected.
   * 
   * @param id the id of the latest failure
   * @throws IOException
   */
  @Override
  public void setId(T id) throws WatchDogException {
    LOGGER.log(Level.INFO, "Storing the latest detected failure in " + file.getName());
    try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
      oos.writeObject(id);
    } catch (IOException e) {
      throw new WatchDogException(e);
    }
  }

}
