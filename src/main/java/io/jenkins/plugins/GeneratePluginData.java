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
import java.util.stream.StreamSupport;
import java.util.zip.GZIPOutputStream;

/**
 * <p>Class that generates the final plugins.json.gzip file that is used for indexing in the embedded Elasticsearch
 * node.</p>
 *
 *
 * This is broken down into a few steps:
 * <ol>
 *   <li>Download plugin information from Jenkins update center</li>
 *   <li>Git clone plugin statistics</li>
 *   <li>Parse the plugin information, matching it with plugin statistics</li>
 *   <li>Persist result to JSON</li>
 * </ol>
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
    final Path statisticsPath = downloadStatistics();
    final List<Plugin> plugins = generatePlugins(pluginsJson, statisticsPath);
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

  private Path downloadStatistics() {
    try {
      final Path tempDir = Files.createTempDirectory("infra-statistics");
      logger.info("Cloning jenkins-infra/infra-statistics");
      Git.cloneRepository().setURI("git://github.com/jenkins-infra/infra-statistics.git").setBranch("gh-pages").setDirectory(tempDir.toFile()).call();
      logger.info("Finished cloning jenkins-infra/infra-statistics");
      return tempDir;
    } catch (Exception e) {
      logger.error("Problem downloading plugin statistics", e);
      throw new RuntimeException(e);
    }
  }

  private List<Plugin> generatePlugins(JSONObject pluginsJson, Path statisticsPath) {
    try {
      final Map<String, String> labelToCategoryMap = buildLabelToCategoryMap();
      final List<Plugin> plugins = new ArrayList<>();
      for (String key : pluginsJson.keySet()) {
        final JSONObject json = pluginsJson.getJSONObject(key);
        final Plugin plugin = parsePlugin(json, statisticsPath, labelToCategoryMap);
        plugins.add(plugin);
      }
      return plugins;
    } catch (Exception e) {
      logger.error("Problem generating plugins", e);
      throw new RuntimeException(e);
    }
  }

  private Plugin parsePlugin(JSONObject json, Path statisticsPath, Map<String, String> labelToCategoryMap) {
    final Plugin plugin = new Plugin();
    plugin.setExcerpt(json.optString("excerpt", null));
    plugin.setGav(json.optString("gav", null));
    plugin.setName(json.getString("name"));
    plugin.setPreviousVersion(json.optString("previousVersion", null));
    plugin.setRequiredCore(json.optString("requiredCore"));
    plugin.setScm(json.optString("scm", null));
    plugin.setSha1(json.optString("sha1", null));
    plugin.setTitle(json.optString("title", null));
    plugin.setUrl(json.optString("url", null));
    plugin.setVersion(json.optString("version", null));
    plugin.setWiki(new Wiki(null, json.optString("wiki", null)));
    final Set<String> categories = new HashSet<>();
    final List<String> labels = new ArrayList<>();
    final JSONArray labelsJson = json.optJSONArray("labels");
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
    final JSONArray dependenciesJson = json.getJSONArray("dependencies");
    if (dependenciesJson != null) {
      for (int i = 0; i < dependenciesJson.length(); i++) {
        final JSONObject dependencyJson = dependenciesJson.getJSONObject(i);
        final Dependency dependency = new Dependency(
          dependencyJson.optString("name", null),
          dependencyJson.optBoolean("optional", false),
          dependencyJson.optString("version", null)
        );
        dependencies.add(dependency);
      }
    }
    plugin.setDependencies(dependencies);
    final List<Maintainer> maintainers = new ArrayList<>();
    final JSONArray developersJson = json.getJSONArray("developers");
    if (developersJson != null) {
      StreamSupport.stream(developersJson.spliterator(), false).forEach((obj) -> {
        final JSONObject developerJson = (JSONObject)obj;
        final String name = developerJson.optString("name", null);
        final String email = developerJson.optString("email", null);
        final String developerId = developerJson.optString("developerId", (name != null ? name : email));
        final Maintainer maintainer = new Maintainer(
          developerId,
          name,
          email
        );
        maintainers.add(maintainer);
      });
    }
    plugin.setMaintainers(maintainers);
    if (json.optString("buildDate", null) != null) {
      final LocalDate buildDate = LocalDate.parse(json.getString("buildDate"), BUILD_DATE_FORMATTER);
      plugin.setBuildDate(buildDate);
    }
    if (json.optString("previousTimestamp", null) != null) {
      final LocalDateTime previousTimestamp = LocalDateTime.parse(json.getString("previousTimestamp"), TIMESTAMP_FORMATTER);
      plugin.setPreviousTimestamp(previousTimestamp);
    }
    if (json.optString("releaseTimestamp", null) != null) {
      final LocalDateTime releaseTimestamp = LocalDateTime.parse(json.getString("releaseTimestamp"), TIMESTAMP_FORMATTER);
      plugin.setReleaseTimestamp(releaseTimestamp);
    }
    final Stats stats = parseStatistics(plugin.getName(), json, statisticsPath);
    plugin.setStats(stats);
    return plugin;
  }

  private Stats parseStatistics(String name, JSONObject json, Path statisticsPath) {
    try {
      final Path file = statisticsPath.resolve(String.format("plugin-installation-trend%c%s.stats.json", File.separatorChar, name));
      final Stats stats = new Stats();
      if (Files.exists(file)) {
        logger.info(String.format("Processing statistics for %s", name));
        final JSONObject statsJson = new JSONObject(Files.lines(file).collect(Collectors.joining("\n")));
        final JSONObject installations = statsJson.getJSONObject("installations");
        final JSONObject installationsPercentage = statsJson.getJSONObject("installationsPercentage");
        final JSONObject installationsPerVersion = statsJson.getJSONObject("installationsPerVersion");
        final JSONObject installationsPercentagePerVersion = statsJson.getJSONObject("installationsPercentagePerVersion");
        stats.setInstallations(installations.keySet().stream().map((timestamp) ->
          new Installation(
            Long.valueOf(timestamp),
            installations.getInt(timestamp)
          )
        ).sorted(Comparator.comparingLong(Installation::getTimestamp)) .collect(Collectors.toList()));
        stats.setInstallationsPercentage(installationsPercentage.keySet().stream().map((timestamp) ->
          new InstallationPercentage(
            Long.valueOf(timestamp),
            installationsPercentage.getDouble(timestamp)
          )
        ).sorted(Comparator.comparing(InstallationPercentage::getTimestamp)).collect(Collectors.toList()));
        stats.setInstallationsPerVersion(installationsPerVersion.keySet().stream().map((version) ->
          new InstallationVersion(
            version,
            installationsPerVersion.getInt(version)
          )
        ).sorted(Comparator.comparing(InstallationVersion::getVersion)).collect(Collectors.toList()));
        stats.setInstallationsPercentagePerVersion(installationsPercentagePerVersion.keySet().stream().map((version) ->
          new InstallationPercentageVersion(
            version,
            installationsPercentagePerVersion.getDouble(version)
          )
        ).sorted(Comparator.comparing(InstallationPercentageVersion::getVersion)).collect(Collectors.toList()));
        stats.setCurrentInstalls(!stats.getInstallations().isEmpty() ? stats.getInstallations().get(stats.getInstallations().size()-1).getTotal() : 0);
        if (stats.getInstallations().size() > 1) {
          final int size = stats.getInstallations().size();
          final long trend = stats.getInstallations().get(size-1).getTotal() - stats.getInstallations().get(size-2).getTotal();
          stats.setTrend(trend);
        }
      } else {
        logger.warn(String.format("No statistics available for %s", name));
      }
      return stats;
    } catch (Exception e) {
      logger.error(String.format("Problem parsing statistics for %s", name), e);
      throw new RuntimeException(e);
    }
  }

  private void writePluginsToFile(List<Plugin> plugins) {
    final File data = Paths.get(System.getProperty("user.dir"), "target", "plugins.json.gzip").toFile();
    try(final Writer writer = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(data)), "utf-8"))) {
      JsonObjectMapper.getObjectMapper().writeValue(writer, new GeneratedPluginData(plugins));
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
