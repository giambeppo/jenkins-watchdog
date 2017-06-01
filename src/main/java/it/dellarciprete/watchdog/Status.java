package it.dellarciprete.watchdog;

/**
 * Represents the status of the phenomenon observed by a watchdog.
 * 
 * @param <T> the type used for identifying an instance of the phenomenon
 */
public abstract class Status<T> {

  private final boolean failure;
  private final T id;

  protected Status(boolean failure, T id) {
    this.failure = failure;
    this.id = id;
  }

  /**
   * If this returns true, an alert must be raised.
   * 
   * @return true if an alert must be raised, false otherwise
   */
  public boolean isFailure() {
    return failure;
  }

  /**
   * Returns the id of this instance of the watched phenomenon.
   * 
   * @return the id
   */
  public T getId() {
    return id;
  }

  /**
   * Returns the set of parameters that should be used to customize an alert message related to this observation, if it
   * is a failure.
   * 
   * @return an array of parameters to customize the failure message
   */
  public abstract Object[] getParams();

}
