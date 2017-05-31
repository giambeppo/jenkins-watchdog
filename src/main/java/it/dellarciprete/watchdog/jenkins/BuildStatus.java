package it.dellarciprete.watchdog.jenkins;

public class BuildStatus {

  private final int number;
  private final boolean success;
  private final String url;

  public BuildStatus(int number, boolean success, String url) {
    this.number = number;
    this.success = success;
    this.url = url;
  }

  public int getNumber() {
    return number;
  }

  public boolean isSuccess() {
    return success;
  }

  public String getUrl() {
    return url;
  }

}
