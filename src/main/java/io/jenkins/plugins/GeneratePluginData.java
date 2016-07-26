package io.jenkins.plugins;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.eclipse.jgit.api.Git;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Class that generates the final plugins.json file that is intended to be used for indexing
 * in the embedded Elasticsearch node.
 */
public class GeneratePluginData {

  private static final Logger logger = LoggerFactory.getLogger(GeneratePluginData.class);

  public static void main(String[] args) {
    final GeneratePluginData generatePluginData = new GeneratePluginData();
    generatePluginData.generate();
  }

  public void generate() {
    final JSONObject plugins = getPlugins();
    getPluginStatistics(plugins);
    writePluginsToFile(plugins);
  }

  private JSONObject getPlugins() {
    final CloseableHttpClient httpClient = HttpClients.createDefault();
    final ResponseHandler<JSONObject> updateCenterHandler = (httpResponse) -> {
      final StatusLine status = httpResponse.getStatusLine();
      if (status.getStatusCode() == 200) {
        final HttpEntity entity = httpResponse.getEntity();
        final String content = EntityUtils.toString(entity);
        try {
          return new JSONObject(String.join("", content.split("\n")[1])).getJSONObject("plugins");
        } catch (Exception e) {
          throw new ClientProtocolException("Update center returned invalid JSON");
        }
      } else {
        throw new ClientProtocolException("Unexpected response from update center - " + status.toString());
      }
    };
    logger.info("Begin downloading plugins from update center");
    try {
      final JSONObject plugins = httpClient.execute(new HttpGet("https://updates.jenkins-ci.org/current/update-center.json"), updateCenterHandler);
      logger.info(String.format("Retrieved %d plugins from update center", plugins.keySet().size()));
      return plugins;
    } catch (Exception e) {
      logger.error("Problem communicating with update center", e);
      throw new RuntimeException(e);
    } finally {
      try {
        httpClient.close();
      } catch (IOException e) {
        logger.error("Problem closing httpClient", e);
      }
    }
  }

  private void getPluginStatistics(JSONObject plugins) {
    try {
      final Path tempDir = Files.createTempDirectory("infra-statistics");
      logger.info("Cloning jenkins-infra/infra-statistics");
      Git.cloneRepository().setURI("git://github.com/jenkins-infra/infra-statistics.git").setBranch("gh-pages").setDirectory(tempDir.toFile()).call();
      logger.info("Finished cloning jenkins-infra/infra-statistics");
      final Map<String, String> labelToCategoryMap = buildLabelToCategoryMap();
      for (String key : plugins.keySet()) {
        final JSONObject plugin = plugins.getJSONObject(key);
        final Path file = tempDir.resolve(String.format("plugin-installation-trend%c%s.stats.json", File.separatorChar, key));
        if (Files.exists(file)) {
          logger.info(String.format("Processing statistics for %s", key));
          final JSONObject stats = new JSONObject();
          final JSONObject json = new JSONObject(Files.lines(file).collect(Collectors.joining("\n")));
          final JSONObject installations = json.getJSONObject("installations");
          final JSONObject installationsPercentage = json.getJSONObject("installationsPercentage");
          final JSONObject installationsPerVersion = json.getJSONObject("installationsPerVersion");
          final JSONObject installationsPercentagePerVersion = json.getJSONObject("installationsPercentagePerVersion");
          stats.put("installations", new JSONArray(installations.keySet().stream().map((timestamp) -> {
            final JSONObject installation = new JSONObject();
            installation.put("timestamp", Long.valueOf(timestamp));
            installation.put("total", installations.getInt(timestamp));
            return installation;
          }).collect(Collectors.toSet())));
          stats.put("installationsPercentage", new JSONArray(installationsPercentage.keySet().stream().map((timestamp) -> {
            final JSONObject installation = new JSONObject();
            installation.put("timestamp", Long.valueOf(timestamp));
            installation.put("percentage", installationsPercentage.getDouble(timestamp));
            return installation;
          }).collect(Collectors.toSet())));
          stats.put("installationsPerVersion", new JSONArray(installationsPerVersion.keySet().stream().map((version) -> {
            final JSONObject installation = new JSONObject();
            installation.put("version", version);
            installation.put("total", installationsPerVersion.getInt(version));
            return installation;
          }).collect(Collectors.toSet())));
          stats.put("installationsPercentagePerVersion", new JSONArray(installationsPercentagePerVersion.keySet().stream().map((version) -> {
            final JSONObject installation = new JSONObject();
            installation.put("version", version);
            installation.put("total", installationsPercentagePerVersion.getDouble(version));
            return installation;
          }).collect(Collectors.toSet())));
          plugin.put("stats", stats);
          final JSONArray categories = new JSONArray();
          final JSONArray labels = plugin.optJSONArray("labels");
          for (int i = 0; i < labels.length(); i++) {
            final String label = labels.getString(i);
            if (labelToCategoryMap.containsKey(label)) {
              categories.put(labelToCategoryMap.get(label));
            }
          }
          plugin.put("categories", categories);
        } else {
          logger.warn(String.format("No statistics for %s found", key));
        }
      }
    } catch (Exception e) {
      logger.error("Problem processing statistics", e);
      throw new RuntimeException(e);
    }
  }

  private void writePluginsToFile(JSONObject plugins) {
    try {
      final File data = Paths.get(System.getProperty("user.dir"), "src", "main", "resources", "elasticsearch", "data", "plugins.json").toFile();
      FileUtils.writeStringToFile(data, plugins.toString(2), "utf-8");
    } catch (Exception e) {
      logger.error("Problem writing plugin data to file", e);
      throw new RuntimeException(e);
    }
  }

  private Map<String, String> buildLabelToCategoryMap() {
    final JSONArray categories;
    final Map<String, String> result = new HashMap<>();
    try {
      final ClassLoader cl = getClass().getClassLoader();
      final File file = new File(cl.getResource("categories.json").getFile());
      categories = new JSONObject(FileUtils.readFileToString(file, "utf-8")).getJSONArray("categories");
    } catch (Exception e) {
      return Collections.emptyMap();
    }
    try {
      for (int i = 0; i < categories.length(); i++) {
        final JSONObject category = categories.getJSONObject(i);
        final JSONArray labels = category.getJSONArray("labels");
        for (int j = 0; j < labels.length(); j++) {
          final JSONObject label = labels.getJSONObject(j);
          result.put(label.getString("id"), category.getString("id"));
        }
      }
      return result;
    } catch (JSONException e) {
      return Collections.emptyMap();
    }
  }

}
