package it.dellarciprete.watchdog.jenkins;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;

import it.dellarciprete.watchdog.Watcher;
import it.dellarciprete.watchdog.common.RestClient;
import it.dellarciprete.watchdog.utils.Configuration;
import it.dellarciprete.watchdog.utils.WatchDogException;
import it.dellarciprete.watchdog.utils.WatchDogLogger;

/**
 * Handles the retrieval of the latest build status for a Jenkins job.
 */
public class JenkinsClient implements Watcher<Integer> {

  private static final Logger LOGGER = WatchDogLogger.get();

  private final RestClient jobStatusRetriever;

  public JenkinsClient(Configuration config) throws WatchDogException {
    String baseUrl = config.get("jenkins.base.url");
    String jobName = config.get("jenkins.job.name");
    String requestUrl = baseUrl + "/job/" + jobName + "/lastBuild/api/json";
    jobStatusRetriever = new RestClient(requestUrl);
  }

  /**
   * Tries to retrieve the status of the latest Jenkins build.
   * 
   * <p>The build is considered to be successful even if it is still running.</p>
   * 
   * @return the latest build status
   * @throws WatchDogException
   */
  @Override
  public BuildStatus getLatestStatus() throws WatchDogException {
    LOGGER.log(Level.INFO, "Sending the request to Jenkins");
    JSONObject json = jobStatusRetriever.retrieveResource();
    LOGGER.log(Level.INFO, "Jenkins response: " + json.toString());
    int id = json.getInt("number");
    String url = json.getString("url");
    String status = json.getString("result");
    boolean building = json.getBoolean("building");
    boolean success = building || "SUCCESS".equals(status);
    return new BuildStatus(id, !success, url);
  }

}
