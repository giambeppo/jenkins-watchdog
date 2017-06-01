package it.dellarciprete.watchdog;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import it.dellarciprete.watchdog.utils.Configuration;
import it.dellarciprete.watchdog.utils.WatchDogLogger;

/**
 * Handles the infinite control loop activating in turn every registered watchdog.
 */
public class WatchDogPen {

  private static final Logger LOGGER = WatchDogLogger.get();
  private static volatile boolean shuttingDown = false;

  private final int pollingInterval;
  private final List<WatchDog<?>> watchdogs;

  public WatchDogPen(Configuration config, WatchDog<?>... watchdogs) {
    pollingInterval = Integer.parseInt(config.get("polling.interval.minutes", "15"));
    this.watchdogs = Arrays.asList(watchdogs);
  }

  /**
   * Runs the infinite control loop.
   *
   * <p>It stops when the suthdown hook is executed.</p>
   */
  public void startControlLoop() {
    Runtime.getRuntime().addShutdownHook(new WatchDogShutdownHook(Thread.currentThread()));
    while (!shuttingDown) {
      LOGGER.log(Level.INFO, "Entering the control loop");
      for (WatchDog<?> watchdog : watchdogs) {
        try {
          watchdog.watch();
        } catch (Exception e) {
          LOGGER.log(Level.SEVERE, "Error while runnning watchdog " + watchdog.getName(), e);
        }
        if (shuttingDown) {
          break;
        }
      }
      LOGGER.log(Level.INFO, "Control loop completed");
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
