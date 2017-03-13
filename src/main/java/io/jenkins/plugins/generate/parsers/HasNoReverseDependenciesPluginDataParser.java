package io.jenkins.plugins.generate.parsers;

import io.jenkins.plugins.generate.PluginDataParser;
import io.jenkins.plugins.models.Plugin;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class HasNoReverseDependenciesPluginDataParser implements PluginDataParser {

  private final Set<String> noReverseDependencies;

  public HasNoReverseDependenciesPluginDataParser(JSONObject updateCenterJson) {
    final JSONObject pluginsJson = updateCenterJson.getJSONObject("plugins");
    noReverseDependencies = buildNoReverseDependencies(pluginsJson);
  }

  @Override
  public void parse(JSONObject pluginJson, Plugin plugin) {
    // WEBSITE-327
    plugin.setHasNoReverseDependencies(noReverseDependencies.contains(plugin.getName()));
  }

  private Set<String> buildNoReverseDependencies(JSONObject pluginsJson) {
    final Map<String, Set<String>> reverseDependencyMap = new HashMap<>();
    pluginsJson.keySet().stream().map(pluginsJson::getJSONObject).forEach(plugin -> {
      final String name = plugin.getString("name");
      StreamSupport.stream(plugin.getJSONArray("dependencies").spliterator(), false)
        .map(obj -> (JSONObject) obj)
        .map(dependency -> dependency.getString("name"))
        .forEach(dependency -> {
          reverseDependencyMap.computeIfAbsent(dependency, k -> new HashSet<>()).add(name);
        });
    });
    return pluginsJson.keySet().stream().filter(key -> !reverseDependencyMap.containsKey(key)).collect(Collectors.toSet());
  }

}
