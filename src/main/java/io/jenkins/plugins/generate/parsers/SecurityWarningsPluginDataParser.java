package io.jenkins.plugins.generate.parsers;

import io.jenkins.plugins.generate.PluginDataParser;
import io.jenkins.plugins.models.Plugin;
import io.jenkins.plugins.models.SecurityWarning;
import io.jenkins.plugins.models.SecurityWarningVersion;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class SecurityWarningsPluginDataParser implements PluginDataParser {

  private final Map<String, List<JSONObject>> nameToWarningsMap;

  public SecurityWarningsPluginDataParser(JSONObject updateCenterJson) {
    final JSONArray warningsJson = updateCenterJson.getJSONArray("warnings");
    nameToWarningsMap = StreamSupport.stream(warningsJson.spliterator(), false)
      .map(obj -> (JSONObject)obj)
      .filter(warning -> warning.getString("type").equalsIgnoreCase("plugin"))
      .collect(Collectors.toMap(warning -> warning.getString("name"), Arrays::asList, (o, n) -> { o.addAll(n); return o; }));
  }

  @Override
  public void parse(JSONObject pluginJson, Plugin plugin) {
    final String name = pluginJson.getString("name");
    final String version = pluginJson.getString("version");
    if (nameToWarningsMap.containsKey(name)) {
      plugin.setSecurityWarnings(nameToWarningsMap.get(name).stream()
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
              .map(versionJson -> version.matches(versionJson.getString("pattern")))
              .reduce(false, (a, b) -> a || b);
          return new SecurityWarning(
            warningJson.getString("id"),
            warningJson.getString("message"),
            warningJson.getString("url"),
            active,
            versions);
        }).collect(Collectors.toList()));
    }
  }

}
