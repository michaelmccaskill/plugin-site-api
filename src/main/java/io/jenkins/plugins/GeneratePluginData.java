package io.jenkins.plugins;

import io.jenkins.plugins.commons.JsonObjectMapper;
import io.jenkins.plugins.models.*;
import io.jenkins.plugins.utils.VersionUtils;
import org.apache.commons.io.FileUtils;
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
import org.eclipse.jgit.api.Git;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
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

  private static final DateTimeFormatter BUILD_DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.US);

  // java.time DateTimeFormatter.ISO_LOCAL_DATE_TIME uses nano-of-second where we're using milliseconds
  private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SS'Z'", Locale.US);

  private static final UrlValidator URL_VALIDATOR = new UrlValidator(new String[] {"http","https"});

  public static void main(String[] args) {
    final GeneratePluginData generatePluginData = new GeneratePluginData();
    generatePluginData.generate();
  }

  public void generate() {
    final Map<String, String> pluginToDocumentationUrlMap = buildPluginToDocumentationUrlMap();
    final JSONObject updateCenterJson = getUpdateCenterJson();
    final JSONObject pluginsJson = updateCenterJson.getJSONObject("plugins");
    final JSONArray warningsJson = updateCenterJson.getJSONArray("warnings");
    final Path statisticsPath = downloadStatistics();
    final List<Plugin> plugins = generatePlugins(pluginsJson, statisticsPath, warningsJson, pluginToDocumentationUrlMap);
    writePluginsToFile(plugins);
  }

  private JSONObject getUpdateCenterJson() {
    final CloseableHttpClient httpClient = HttpClients.createDefault();
    final ResponseHandler<JSONObject> updateCenterHandler = httpResponse -> {
      final StatusLine status = httpResponse.getStatusLine();
      if (status.getStatusCode() == 200) {
        final HttpEntity entity = httpResponse.getEntity();
        final String content = EntityUtils.toString(entity, StandardCharsets.UTF_8);
        try {
          return new JSONObject(String.join("", content.split("\n")[1]));
        } catch (Exception e) {
          throw new ClientProtocolException("Update center returned invalid JSON");
        }
      } else {
        throw new ClientProtocolException("Unexpected response from update center - " + status.toString());
      }
    };
    logger.info("Begin downloading plugins from update center");
    try {
      return httpClient.execute(new HttpGet("https://updates.jenkins.io/current/update-center.json"), updateCenterHandler);
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

  private List<Plugin> generatePlugins(JSONObject pluginsJson, Path statisticsPath, JSONArray warningsJson,
                                       Map<String, String> pluginToDocumentationUrlMap) {
    try {
      final Map<String, String> labelToCategoryMap = buildLabelToCategoryMap();
      final Map<String, String> dependencyNameToTitleMap = buildDependencyNameToTitleMap(pluginsJson);
      final Map<String, List<JSONObject>> warnings = buildNameToWarningsMap(warningsJson);
      return pluginsJson.keySet().stream()
        .map(pluginsJson::getJSONObject)
        .map(json -> parsePlugin(json, statisticsPath, labelToCategoryMap, dependencyNameToTitleMap, warnings, pluginToDocumentationUrlMap))
        .collect(Collectors.toList());
    } catch (Exception e) {
      logger.error("Problem generating plugins", e);
      throw new RuntimeException(e);
    }
  }

  private Plugin parsePlugin(JSONObject json, Path statisticsPath, Map<String, String> labelToCategoryMap,
                             Map<String, String> dependencyNameToTitleMap, Map<String, List<JSONObject>> warningsMap,
                             Map<String, String> pluginToDocumentationUrlMap) {
    final Plugin plugin = new Plugin();
    plugin.setExcerpt(json.optString("excerpt", null));
    plugin.setGav(json.optString("gav", null));
    plugin.setName(json.getString("name"));
    plugin.setPreviousVersion(json.optString("previousVersion", null));
    plugin.setRequiredCore(json.optString("requiredCore"));
    plugin.setSha1(json.optString("sha1", null));
    plugin.setTitle(json.optString("title", null));
    plugin.setUrl(json.optString("url", null));
    plugin.setVersion(json.optString("version", null));
    plugin.setWiki(parseWiki(plugin.getName(), pluginToDocumentationUrlMap));
    final List<String> labels = StreamSupport.stream(json.optJSONArray("labels").spliterator(), false)
      .map(obj -> (String)obj)
      .collect(Collectors.toList());
    final Set<String> categories = labels.stream()
      .filter(labelToCategoryMap::containsKey)
      .map(labelToCategoryMap::get)
      .collect(Collectors.toSet());
    plugin.setLabels(labels);
    plugin.setCategories(new ArrayList<>(categories));
    final List<Dependency> dependencies = StreamSupport.stream(json.optJSONArray("dependencies").spliterator(), false)
      .map(obj -> (JSONObject)obj)
      .map(dependencyJson -> {
        final String name = dependencyJson.getString("name");
        final String title = dependencyNameToTitleMap.getOrDefault(name, name);
        return new Dependency(
          name,
          title,
          dependencyJson.optBoolean("optional", false),
          dependencyJson.optString("version", null)
        );
      }).collect(Collectors.toList());
    plugin.setDependencies(dependencies);
    final List<Maintainer> maintainers = StreamSupport.stream(json.optJSONArray("developers").spliterator(), false)
      .map(obj -> (JSONObject)obj)
      .map(developerJson -> {
        final String name = developerJson.optString("name", null);
        final String email = developerJson.optString("email", null);
        final String developerId = developerJson.optString("developerId", (name != null ? name : email));
        return new Maintainer(
          developerId,
          name,
          email
        );
      }).collect(Collectors.toList());
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
    final Scm scm = parseScm(plugin, json.optString("scm", ""));
    plugin.setScm(scm);
    if (warningsMap.containsKey(plugin.getName())) {
      final List<SecurityWarning> warnings = warningsMap.get(plugin.getName()).stream()
        .map(warningJson -> {
          final List<SecurityWarningVersion> versions =
            StreamSupport.stream(warningJson.getJSONArray("versions").spliterator(), false)
              .map(obj -> (JSONObject) obj)
              .map(versionJson -> new SecurityWarningVersion(
                versionJson.optString("firstVersion", null),
                versionJson.optString("lastVersion", null)
              ))
              .collect(Collectors.toList());
          final boolean active =
            StreamSupport.stream(warningJson.getJSONArray("versions").spliterator(), false)
              .map(obj -> (JSONObject) obj)
              .map(versionJson -> plugin.getVersion().matches(versionJson.getString("pattern")))
              .reduce(false, (a, b) -> a || b);
          return new SecurityWarning(
                warningJson.getString("id"),
                warningJson.getString("message"),
                warningJson.getString("url"),
                active,
                versions);
        }).collect(Collectors.toList());
      plugin.setSecurityWarnings(warnings);
    }
    return plugin;
  }

  private Scm parseScm(Plugin plugin, String scmString) {
    final Scm scm = new Scm();
    final String name = plugin.getName().endsWith("-plugin") ? plugin.getName() : plugin.getName() + "-plugin";
    final String issues = "http://issues.jenkins-ci.org/secure/IssueNavigator.jspa?mode=hide&reset=true&jqlQuery=project+%3D+JENKINS+AND+status+in+%28Open%2C+%22In+Progress%22%2C+Reopened%29+AND+component+%3D+%27" + name + "%27";
    scm.setIssues(issues);
    if (scmString.endsWith("github.com")) {
      final String link = "https://github.com/jenkinsci/" + name;
      final String baseCompareUrl = String.format("%s/compare/%s-", link, plugin.getName());
      final String inLatestRelease = String.format("%s%s...%s-%s", baseCompareUrl, plugin.getPreviousVersion(), plugin.getName(), plugin.getVersion());
      final String sinceLatestRelease = String.format("%s%s...master", baseCompareUrl, plugin.getVersion());
      final String pullRequests = link + "/pulls";
      scm.setLink(link);
      scm.setInLatestRelease(inLatestRelease);
      scm.setSinceLatestRelease(sinceLatestRelease);
      scm.setPullRequests(pullRequests);
    }
    return scm;
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
        stats.setInstallations(installations.keySet().stream().map(timestamp ->
          new Installation(
            Long.valueOf(timestamp),
            installations.getInt(timestamp)
          )
        ).sorted(Comparator.comparingLong(Installation::getTimestamp)).collect(Collectors.toList()));
        stats.setInstallationsPercentage(installationsPercentage.keySet().stream().map(timestamp ->
          new InstallationPercentage(
            Long.valueOf(timestamp),
            installationsPercentage.getDouble(timestamp)
          )
        ).sorted(Comparator.comparing(InstallationPercentage::getTimestamp)).collect(Collectors.toList()));
        stats.setInstallationsPerVersion(installationsPerVersion.keySet().stream().map(version ->
          new InstallationVersion(
            version,
            installationsPerVersion.getInt(version)
          )
        ).sorted(Comparator.comparing(InstallationVersion::getVersion)).collect(Collectors.toList()));
        stats.setInstallationsPercentagePerVersion(installationsPercentagePerVersion.keySet().stream().map(version ->
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

  private Wiki parseWiki(String plugin, Map<String, String> pluginToDocumentationUrlMap) {
    final String url = verifyWikiBlacklist(pluginToDocumentationUrlMap.getOrDefault(plugin, null));
    return new Wiki(null, url);
  }

  private String verifyWikiBlacklist(String url) {
    if (StringUtils.isBlank(url)) {
      return url;
    }
    return URL_VALIDATOR.isValid(url) ? url : null;
  }

  private void writePluginsToFile(List<Plugin> plugins) {
    final File data = Paths.get(System.getProperty("user.dir"), "target", "plugins.json.gzip").toFile();
    try(final Writer writer = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(data)), StandardCharsets.UTF_8))) {
      final String mappingVersion = VersionUtils.getMappingVersion();
      final String elasticsearchVersion = VersionUtils.getElasticsearchVersion();
      JsonObjectMapper.getObjectMapper().writeValue(writer, new GeneratedPluginData(plugins, mappingVersion, elasticsearchVersion));
    } catch (Exception e) {
      logger.error("Problem writing plugin data to file", e);
      throw new RuntimeException(e);
    }
  }

  private Map<String, String> buildLabelToCategoryMap() {
    try {
      final ClassLoader cl = getClass().getClassLoader();
      final File file = new File(cl.getResource("categories.json").getFile());
      final JSONArray categories = new JSONObject(FileUtils.readFileToString(file, StandardCharsets.UTF_8)).getJSONArray("categories");
      return StreamSupport.stream(categories.spliterator(), false)
        .map(obj -> (JSONObject)obj)
        .map(category -> StreamSupport.stream(category.getJSONArray("labels").spliterator(), false)
          .map(obj -> (String)obj)
          .collect(Collectors.toMap(Function.identity(), label -> category.getString("id")))
        )
        .flatMap(map -> map.entrySet().stream())
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    } catch (Exception e) {
      return Collections.emptyMap();
    }
  }

  private Map<String, String> buildDependencyNameToTitleMap(JSONObject pluginsJson) {
    return pluginsJson.keySet().stream()
      .map(pluginsJson::getJSONObject)
      .collect(Collectors.toMap(plugin -> plugin.getString("name"), plugin -> plugin.getString("title")));
  }

  private Map<String, List<JSONObject>> buildNameToWarningsMap(JSONArray warningsJson) {
    return StreamSupport.stream(warningsJson.spliterator(), false)
      .map(obj -> (JSONObject)obj)
      .filter(warning -> warning.getString("type").equalsIgnoreCase("plugin"))
      .collect(Collectors.toMap(warning -> warning.getString("name"), Arrays::asList, (o, n) -> { o.addAll(n); return o; }));
  }

  private Map<String, String> buildPluginToDocumentationUrlMap() {
    if (System.getenv().containsKey("PLUGIN_DOCUMENTATION_URL")) {
      final String url = StringUtils.trimToNull(System.getenv("PLUGIN_DOCUMENTATION_URL"));
      if (url == null) {
        throw new RuntimeException("Environment variable 'PLUGIN_DOCUMENTATION_URL' is empty");
      }
      final CloseableHttpClient httpClient = HttpClients.createDefault();
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
            logger.error("{} returned invalid JSON", url, e);
            throw new ClientProtocolException(String.format("%s returned invalid JSON", url));
          }
        } else {
          throw new ClientProtocolException(String.format("Unexpected response from %s - %s", url, status.toString()));
        }
      };
      try {
        return httpClient.execute(new HttpGet(url), handler);
      } catch (Exception e) {
        logger.error("Problem communicating with {}", url, e);
        return Collections.emptyMap();
      } finally {
        try {
          httpClient.close();
        } catch (IOException e) {
          logger.error("Problem closing httpClient", e);
        }
      }
    } else {
      throw new RuntimeException("Environment variable 'PLUGIN_DOCUMENTATION_URL' is required");
    }
  }

}
