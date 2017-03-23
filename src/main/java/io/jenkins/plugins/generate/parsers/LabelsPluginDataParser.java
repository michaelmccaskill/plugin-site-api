package io.jenkins.plugins.generate.parsers;

import io.jenkins.plugins.generate.PluginDataParser;
import io.jenkins.plugins.models.Plugin;
import org.json.JSONObject;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class LabelsPluginDataParser implements PluginDataParser {

  @Override
  public void parse(JSONObject pluginJson, Plugin plugin) {
    plugin.setLabels(StreamSupport.stream(pluginJson.optJSONArray("labels").spliterator(), false)
      .map(obj -> (String)obj)
      .collect(Collectors.toSet()));
  }

}
