package it.dellarciprete.watchdog.jenkins;

public class WatchDogException extends Exception {

  private static final long serialVersionUID = 545840949541067644L;

  public WatchDogException(String message) {
    super(message);
  }

  public WatchDogException(String message, Throwable ex) {
    super(message, ex);
  }

}
