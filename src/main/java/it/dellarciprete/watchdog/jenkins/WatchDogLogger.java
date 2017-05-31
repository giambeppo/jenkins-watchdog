package it.dellarciprete.watchdog.jenkins;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Handles the application logger.
 */
public class WatchDogLogger {

  private static Logger logger;
  private static WatchDogLogManager logManager;

  static {
    // must be invoked before using any Logger
    System.setProperty("java.util.logging.manager", WatchDogLogManager.class.getName());
  }

  /**
   * Retrieved the application logger.
   * 
   * <p>The application logger writes to a file and ignored the shutdown signal, allowing logging to go on during the
   * execution of shutdown hooks. This requires manual finalization.</p>
   * 
   * @return the logger
   */
  public static Logger get() {
    if (logger == null) {
      logger = Logger.getLogger("watchdog");
      try {
        FileHandler fh = new FileHandler("watchdog.log");
        logger.addHandler(fh);
        SimpleFormatter formatter = new SimpleFormatter();
        fh.setFormatter(formatter);
      } catch (SecurityException | IOException e) {
        logger.log(Level.SEVERE, "Unable to redirect logs to file", e);
      }
    }
    return logger;
  }

  /**
   * Esplicitly finalizes the application logger, which is not autmatically finalized upon receiving the shutdown
   * signal.
   */
  public static void finalizeLogger() {
    logManager.finalizeLogger();
  }

  private WatchDogLogger() {
    throw new UnsupportedOperationException("Utility class, not to be instantiated");
  }

  /**
   * LogManager implementation that prevents finalization upon receiving the shutdown signal and allows manual
   * finalization instead.
   */
  public static class WatchDogLogManager extends LogManager {

    public WatchDogLogManager() {
      logManager = this;
    }

    @Override
    public void reset() {
      // Doing nothing, logs should still be used during shutdown process
    }

    private void finalizeLogger() {
      super.reset();
    }

  }

}
