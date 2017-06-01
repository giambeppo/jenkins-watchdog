package it.dellarciprete.watchdog.sonarqube;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;

import it.dellarciprete.watchdog.Watcher;
import it.dellarciprete.watchdog.common.RestClient;
import it.dellarciprete.watchdog.utils.Configuration;
import it.dellarciprete.watchdog.utils.WatchDogException;
import it.dellarciprete.watchdog.utils.WatchDogLogger;

public class SonarQubeClient implements Watcher<String> {

  private static final Logger LOGGER = WatchDogLogger.get();

  private final RestClient qualityGateRetriever;
  private final RestClient componentRetriever;
  private final String dashboardUrl;

  public SonarQubeClient(Configuration config) throws WatchDogException {
    String baseUrl = config.get("sonarqube.base.url");
    String projectKey = config.get("sonarqube.project.key");
    String qgUrl = baseUrl + "/api/qualitygates/project_status?projectKey=" + projectKey;
    qualityGateRetriever = new RestClient(qgUrl);
    String compUrl = baseUrl + "/api/ce/component?componentKey=" + projectKey;
    componentRetriever = new RestClient(compUrl);
    dashboardUrl = baseUrl + "/dashboard?id=" + projectKey;
  }

  @Override
  public AnalysisStatus getLatestStatus() throws WatchDogException {
    LOGGER.log(Level.INFO, "Sending the request to SonarQube");
    JSONObject compInfo = componentRetriever.retrieveResource();
    LOGGER.log(Level.INFO, "SonarQube component response: " + compInfo.toString());
    AnalysisStatus status;
    if (compInfo.getJSONArray("queue").length() > 0) {
      LOGGER.log(Level.INFO, "SonarQube analysis in progress, marking as success");
      // We still don't have the new analysis' id, but we don't care since this is not a failure
      status = new AnalysisStatus(false, "dummy", dashboardUrl);
    } else {
      JSONObject qgInfo = qualityGateRetriever.retrieveResource();
      LOGGER.log(Level.INFO, "SonarQube quality gate response: " + qgInfo.toString());
      String qgStatus = qgInfo.getJSONObject("projectStatus").getString("status");
      boolean qgPassed = "OK".equals(qgStatus);
      String analysisId = compInfo.getJSONObject("current").getString("analysisId");
      status = new AnalysisStatus(!qgPassed, analysisId, dashboardUrl);
    }
    return status;
  }

}
