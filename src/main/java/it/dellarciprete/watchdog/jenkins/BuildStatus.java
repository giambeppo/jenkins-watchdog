package it.dellarciprete.watchdog.jenkins;

import it.dellarciprete.watchdog.Status;

public class BuildStatus implements Status<Integer> {

  private final Integer number;
  private final boolean failure;
  private final String url;

  public BuildStatus(Integer number, boolean failure, String url) {
    this.number = number;
    this.failure = failure;
    this.url = url;
  }

  public Integer getId() {
    return number;
  }

  public boolean isFailure() {
    return failure;
  }

  @Override
  public Object[] getParams() {
    return new Object[] { url };
  }

}
