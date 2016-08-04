package io.jenkins.plugins;

import io.jenkins.plugins.commons.JsonObjectMapper;
import io.jenkins.plugins.models.*;
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

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;

/**
 * Class that generates the final plugins.json file that is intended to be used for indexing
 * in the embedded Elasticsearch node.
 */
public class GeneratePluginData {

  private static final Logger logger = LoggerFactory.getLogger(GeneratePluginData.class);

  private static final DateTimeFormatter BUILD_DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");

  // java.time DateTimeFormatter.ISO_LOCAL_DATE_TIME uses nano-of-second where we're using milliseconds
  private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SS'Z'");

  public static void main(String[] args) {
    final GeneratePluginData generatePluginData = new GeneratePluginData();
    generatePluginData.generate();
  }

  public void generate() {
    final JSONObject pluginsJson = getPlugins();
    final List<Plugin> plugins = generatePlugins(pluginsJson);
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

  private List<Plugin> generatePlugins(JSONObject pluginsJson) {
    try {
      final Path tempDir = Files.createTempDirectory("infra-statistics");
      logger.info("Cloning jenkins-infra/infra-statistics");
      Git.cloneRepository().setURI("git://github.com/jenkins-infra/infra-statistics.git").setBranch("gh-pages").setDirectory(tempDir.toFile()).call();
      logger.info("Finished cloning jenkins-infra/infra-statistics");
      final Map<String, String> labelToCategoryMap = buildLabelToCategoryMap();
      final List<Plugin> plugins = new ArrayList<>();
      for (String key : pluginsJson.keySet()) {
        final JSONObject pluginJson = pluginsJson.getJSONObject(key);
        final Plugin plugin = new Plugin();
        plugin.setExcerpt(pluginJson.optString("excerpt", null));
        plugin.setGav(pluginJson.optString("gav", null));
        plugin.setName(pluginJson.getString("name"));
        plugin.setPreviousVersion(pluginJson.optString("previousVersion", null));
        plugin.setRequiredCore(pluginJson.optString("requiredCore"));
        plugin.setScm(pluginJson.optString("scm", null));
        plugin.setSha1(pluginJson.optString("sha1", null));
        plugin.setTitle(pluginJson.optString("title", null));
        plugin.setUrl(pluginJson.optString("url", null));
        plugin.setVersion(pluginJson.optString("version", null));
        plugin.setWiki(new Wiki(null, pluginJson.optString("wiki", null)));
        final Set<String> categories = new HashSet<>();
        final List<String> labels = new ArrayList<>();
        final JSONArray labelsJson = pluginJson.optJSONArray("labels");
        if (labelsJson != null) {
          for (int i = 0; i < labelsJson.length(); i++) {
            final String label = labelsJson.getString(i);
            if (labelToCategoryMap.containsKey(label)) {
              categories.add(labelToCategoryMap.get(label));
            }
            labels.add(label);
          }
        }
        plugin.setLabels(labels);
        plugin.setCategories(new ArrayList<>(categories));
        final List<Dependency> dependencies = new ArrayList<>();
        final JSONArray dependenciesJson = pluginJson.getJSONArray("dependencies");
        if (dependenciesJson != null) {
          for (int i = 0; i < dependenciesJson.length(); i++) {
            final JSONObject json = dependenciesJson.getJSONObject(i);
            final Dependency dependency = new Dependency(
              json.optString("name", null),
              json.optBoolean("optional", false),
              json.optString("version", null)
            );
            dependencies.add(dependency);
          }
        }
        plugin.setDependencies(dependencies);
        final List<Developer> developers = new ArrayList<>();
        final JSONArray developersJson = pluginJson.getJSONArray("developers");
        if (developersJson != null) {
          for (int i = 0; i < developersJson.length(); i++) {
            final JSONObject json = developersJson.getJSONObject(i);
            final Developer developer = new Developer(
              json.optString("developerId", null),
              json.optString("name", null),
              json.optString("email", null)
            );
            developers.add(developer);
          }
        }
        plugin.setDevelopers(developers);
        if (pluginJson.optString("buildDate", null) != null) {
          final LocalDate buildDate = LocalDate.parse(pluginJson.getString("buildDate"), BUILD_DATE_FORMATTER);
          plugin.setBuildDate(buildDate);
        }
        if (pluginJson.optString("previousTimestamp", null) != null) {
          final LocalDateTime previousTimestamp = LocalDateTime.parse(pluginJson.getString("previousTimestamp"), TIMESTAMP_FORMATTER);
          plugin.setPreviousTimestamp(previousTimestamp);
        }
        if (pluginJson.optString("releaseTimestamp", null) != null) {
          final LocalDateTime releaseTimestamp = LocalDateTime.parse(pluginJson.getString("releaseTimestamp"), TIMESTAMP_FORMATTER);
          plugin.setReleaseTimestamp(releaseTimestamp);
        }
        final Path file = tempDir.resolve(String.format("plugin-installation-trend%c%s.stats.json", File.separatorChar, key));
        final Stats stats = new Stats();
        if (Files.exists(file)) {
          logger.info(String.format("Processing statistics for %s", key));
          final JSONObject json = new JSONObject(Files.lines(file).collect(Collectors.joining("\n")));
          final JSONObject installations = json.getJSONObject("installations");
          final JSONObject installationsPercentage = json.getJSONObject("installationsPercentage");
          final JSONObject installationsPerVersion = json.getJSONObject("installationsPerVersion");
          final JSONObject installationsPercentagePerVersion = json.getJSONObject("installationsPercentagePerVersion");
          stats.setInstallations(installations.keySet().stream().map((timestamp) -> {
            return new Installation(
              Long.valueOf(timestamp),
              installations.getInt(timestamp)
            );
          }).collect(Collectors.toList()));
          stats.setInstallationsPercentage(installationsPercentage.keySet().stream().map((timestamp) -> {
            return new InstallationPercentage(
              Long.valueOf(timestamp),
              installationsPercentage.getDouble(timestamp)
            );
          }).collect(Collectors.toList()));
          stats.setInstallationsPercentagePerVersion(installationsPerVersion.keySet().stream().map((version) -> {
            return new InstallationPercentageVersion(
              version,
              installationsPerVersion.getInt(version)
            );
          }).collect(Collectors.toList()));
          stats.setInstallationsPercentagePerVersion(installationsPercentagePerVersion.keySet().stream().map((version) -> {
            return new InstallationPercentageVersion(
              version,
              installationsPercentagePerVersion.getDouble(version)
            );
          }).collect(Collectors.toList()));
          final String lifetime = installations.keySet().stream().max(String::compareTo).orElse(null);
          stats.setLifetime(lifetime != null ? installations.getInt(lifetime) : 0);
        }
        plugin.setStats(stats);
        plugins.add(plugin);
      }
      return plugins;
    } catch (Exception e) {
      logger.error("Problem generating plugins", e);
      throw new RuntimeException(e);
    }
  }

  private void writePluginsToFile(List<Plugin> plugins) {
    final File data = Paths.get(System.getProperty("user.dir"), "src", "main", "resources", "elasticsearch", "data", "plugins.json.gzip").toFile();
    try(final Writer writer = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(data)), "utf-8"))) {
      JsonObjectMapper.getObjectMapper().writeValue(writer, plugins);
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
          result.put(labels.getString(j), category.getString("id"));
        }
      }
      return result;
    } catch (JSONException e) {
      return Collections.emptyMap();
    }
  }

}
