package io.jenkins.plugins.generate.parsers;

import io.jenkins.plugins.generate.PluginDataParser;
import io.jenkins.plugins.models.Dependency;
import io.jenkins.plugins.models.Plugin;
import org.json.JSONObject;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class DependenciesPluginDataParser implements PluginDataParser {

  private final Map<String, String> dependencyNameToTitleMap;

  public DependenciesPluginDataParser(JSONObject updateCenterJson) {
    final JSONObject pluginsJson = updateCenterJson.getJSONObject("plugins");
    dependencyNameToTitleMap = buildDependencyNameToTitleMap(pluginsJson);
  }

  @Override
  public void parse(JSONObject pluginJson, Plugin plugin) {
    plugin.setDependencies(StreamSupport.stream(pluginJson.optJSONArray("dependencies").spliterator(), false)
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
      }).collect(Collectors.toList()));
  }

  private Map<String, String> buildDependencyNameToTitleMap(JSONObject pluginsJson) {
    return pluginsJson.keySet().stream()
      .map(pluginsJson::getJSONObject)
      .collect(Collectors.toMap(plugin -> plugin.getString("name"), plugin -> plugin.getString("title")));
  }

}
