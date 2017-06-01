package it.dellarciprete.watchdog.jenkins;

import it.dellarciprete.watchdog.Status;

public class BuildStatus extends Status<Integer> {

  private final String url;

  public BuildStatus(Integer number, boolean failure, String url) {
    super(failure, number);
    this.url = url;
  }

  @Override
  public Object[] getParams() {
    return new Object[] { url };
  }

}
