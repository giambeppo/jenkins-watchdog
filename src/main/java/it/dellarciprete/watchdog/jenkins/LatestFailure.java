package it.dellarciprete.watchdog.jenkins;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Keeps track of the latest build failure detected.
 */
public class LatestFailure {

  private static final Logger LOGGER = WatchDogLogger.get();

  private final File file;

  public LatestFailure(Configuration config) {
    file = new File(config.get("jenkins.file.latest.failure", "latestFailure.ser"));
  }

  /**
   * Retrieves the id of the latest build failure detected.
   * 
   * @return the id of the latest build failure, null if no failure has been detected yet
   * @throws IOException
   */
  public Integer getId() throws IOException {
    LOGGER.log(Level.INFO, "Retrieving latest build failure detected");
    Integer latestFailure = null;
    if (file.exists()) {
      try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
        latestFailure = ois.readInt();
      }
    }
    return latestFailure;
  }

  /**
   * Set the id of the latest build failure detected.
   * 
   * @param number the id of the latest build failure
   * @throws IOException
   */
  public void setId(int number) throws IOException {
    LOGGER.log(Level.INFO, "Setting the latest build failure");
    try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
      oos.writeInt(number);
    }
  }

}
