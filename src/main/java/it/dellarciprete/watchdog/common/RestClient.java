package it.dellarciprete.watchdog.common;

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
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import it.dellarciprete.watchdog.utils.WatchDogException;
import it.dellarciprete.watchdog.utils.WatchDogLogger;

public class RestClient {

  private static final Logger LOGGER = WatchDogLogger.get();

  private final HttpClient client;
  private final String requestUrl;

  public RestClient(String requestUrl) {
    client = new DefaultHttpClient();
    this.requestUrl = requestUrl;
  }

  public JSONObject retrieveResource() throws WatchDogException {
    HttpGet request = new HttpGet(requestUrl);
    HttpResponse response;
    try {
      response = client.execute(request);
    } catch (IOException e) {
      throw new WatchDogException("Unable to retrieve " + requestUrl, e);
    }
    if (response.getStatusLine().getStatusCode() != 200) {
      try {
        EntityUtils.consume(response.getEntity());
      } catch (IOException e) {
        LOGGER.log(Level.WARNING, "Unable to fully consume the response", e);
      }
      throw new WatchDogException(String.format("Error sending the request to URL %s: HTTP code %s", requestUrl,
          response.getStatusLine().getStatusCode()));
    }
    JSONObject json;
    try {
      json = new JSONObject(parseResponse(response));
    } catch (JSONException | IOException e) {
      throw new WatchDogException("Unable to parse the response from " + requestUrl, e);
    }
    return json;
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
