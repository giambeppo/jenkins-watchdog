package it.dellarciprete.watchdog.jenkins;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import it.dellarciprete.watchdog.Watcher;
import it.dellarciprete.watchdog.utils.Configuration;
import it.dellarciprete.watchdog.utils.WatchDogException;
import it.dellarciprete.watchdog.utils.WatchDogLogger;

/**
 * Handles the retrieval of the latest build status for a Jenkins job.
 */
public class JenkinsClient implements Watcher<Integer> {

  private static final Logger LOGGER = WatchDogLogger.get();

  private final HttpClient client;
  private final String requestUrl;

  public JenkinsClient(Configuration config) throws WatchDogException {
    client = new DefaultHttpClient();
    String baseUrl = config.get("jenkins.base.url");
    String jobName = config.get("jenkins.job.name");
    requestUrl = baseUrl + "/job/" + jobName + "/lastBuild/api/json";
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
    HttpGet request = new HttpGet(requestUrl);
    HttpResponse response;
    try {
      response = client.execute(request);
    } catch (IOException e) {
      throw new WatchDogException("Unable to send the request to Jenkins", e);
    }
    if (response.getStatusLine().getStatusCode() != 200) {
      throw new WatchDogException(String.format("Error sending the request to Jenkins (%s): HTTP code %s", requestUrl,
          response.getStatusLine().getStatusCode()));
    }
    JSONObject json;
    try {
      json = new JSONObject(parseResponse(response));
    } catch (JSONException | IOException e) {
      throw new WatchDogException("Unable to parse the response coming from Jenkins", e);
    }
    LOGGER.log(Level.INFO, "Jenkins response: " + json.toString());
    int id = json.getInt("number");
    String url = json.getString("url");
    String status = json.getString("result");
    boolean building = json.getBoolean("building");
    boolean success = building || "SUCCESS".equals(status);
    return new BuildStatus(id, !success, url);
  }

  private String parseResponse(HttpResponse response) throws IOException {
    HttpEntity entity = response.getEntity();
    try (BufferedReader bReader = new BufferedReader(new InputStreamReader(entity.getContent(), "UTF-8"))) {
      StringBuilder builder = new StringBuilder();
      String line = null;
      while ((line = bReader.readLine()) != null) {
        builder.append(line);
      }
      return builder.toString();
    }
  }

}
