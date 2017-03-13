package io.jenkins.plugins.generate.parsers;

import io.jenkins.plugins.generate.PluginDataParser;
import io.jenkins.plugins.models.Plugin;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class FirstReleasePluginDataParser implements PluginDataParser {

  private static final Logger logger = LoggerFactory.getLogger(FirstReleasePluginDataParser.class);

  private static final String URL = "https://updates.jenkins.io/current/release-history.json";

  private final Map<String, LocalDateTime> gavToFirstReleaseMap;

  public FirstReleasePluginDataParser() {
    gavToFirstReleaseMap = buildGavToFirstReleaseMap();
  }

  @Override
  public void parse(JSONObject pluginJson, Plugin plugin) {
    // WEBSITE-309
    final String gav = StringUtils.trimToNull(pluginJson.optString("gav", null));
    plugin.setFirstRelease(gavToFirstReleaseMap.getOrDefault(getGavKey(gav), null));
  }

  private String getGavKey(String gav) {
    return StringUtils.substringBeforeLast(gav, ":");
  }

  private Map<String, LocalDateTime> buildGavToFirstReleaseMap() {
    final ResponseHandler<Map<String, LocalDateTime>> handler = httpResponse -> {
      final StatusLine status = httpResponse.getStatusLine();
      if (status.getStatusCode() == 200) {
        final HttpEntity entity = httpResponse.getEntity();
        final String content = EntityUtils.toString(entity, StandardCharsets.UTF_8);
        try {
          final JSONObject json = new JSONObject(content);
          final JSONArray releaseHistory = json.getJSONArray("releaseHistory");
          // Flatten out "releaseHistory.releases" to "releases" where "releases.firstRelease" is true
          return StreamSupport.stream(releaseHistory.spliterator(), false)
            .map(obj -> (JSONObject) obj)
            .map(entry -> {
              final JSONArray releases = entry.getJSONArray("releases");
              return StreamSupport.stream(releases.spliterator(), false)
                .map(obj -> (JSONObject) obj)
                .filter(release -> release.optBoolean("firstRelease", false) && release.has("gav"))
                .collect(Collectors.toMap(
                  release -> getGavKey(release.getString("gav")),
                  release -> LocalDateTime.from(Instant.ofEpochMilli(release.getLong("timestamp")).atZone(ZoneId.of("UTC")))));
            })
            .collect(Collectors.toList()).stream()
            .flatMap(map -> map.entrySet().stream())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        } catch (Exception e) {
          logger.error("{} returned invalid JSON", URL, e);
          throw new ClientProtocolException(String.format("%s returned invalid JSON", URL));
        }
      } else {
        throw new ClientProtocolException(String.format("Unexpected response from %s - %s", URL, status.toString()));
      }
    };
    try (final CloseableHttpClient httpClient = HttpClients.createDefault()) {
      return httpClient.execute(new HttpGet(URL), handler);
    } catch (Exception e) {
      logger.error("Problem communicating with {}", URL, e);
      return Collections.emptyMap();
    }
  }
}
