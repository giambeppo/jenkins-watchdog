package it.dellarciprete.watchdog.sonarqube;

import it.dellarciprete.watchdog.Status;

public class AnalysisStatus extends Status<String> {

  private final String url;

  protected AnalysisStatus(boolean failure, String id, String url) {
    super(failure, id);
    this.url = url;
  }

  @Override
  public Object[] getParams() {
    return new Object[] { url };
  }

}
