package io.jenkins.plugins.generate.parsers;

import io.jenkins.plugins.generate.PluginDataParser;
import io.jenkins.plugins.models.Plugin;
import io.jenkins.plugins.models.Wiki;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class WikiPluginDataParser implements PluginDataParser {

  private static final Logger logger = LoggerFactory.getLogger(WikiPluginDataParser.class);

  private static final String URL = "https://updates.jenkins.io/current/plugin-documentation-urls.json";

  private static final UrlValidator URL_VALIDATOR = new UrlValidator(new String[] {"http","https"});

  private final Map<String, String> pluginToDocumentationUrlMap;

  public WikiPluginDataParser() {
    pluginToDocumentationUrlMap = buildPluginToDocumentationUrlMap();
  }

  @Override
  public void parse(JSONObject pluginJson, Plugin plugin) {
    final String url = verifyWikiBlacklist(pluginToDocumentationUrlMap.getOrDefault(pluginJson.getString("name"), null));
    plugin.setWiki(new Wiki(null, url));
  }

  private String verifyWikiBlacklist(String url) {
    return URL_VALIDATOR.isValid(url) ? url : null;
  }

  private Map<String, String> buildPluginToDocumentationUrlMap() {
    final ResponseHandler<Map<String, String>> handler = httpResponse -> {
      final StatusLine status = httpResponse.getStatusLine();
      if (status.getStatusCode() == 200) {
        final HttpEntity entity = httpResponse.getEntity();
        final String content = EntityUtils.toString(entity, StandardCharsets.UTF_8);
        try {
          final JSONObject json = new JSONObject(content);
          return json.keySet().stream()
            .filter(key -> StringUtils.isNotBlank(json.getJSONObject(key).optString("url", null)))
            .collect(Collectors.toMap(Function.identity(), key -> json.getJSONObject(key).getString("url")));
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
