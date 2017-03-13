package io.jenkins.plugins.generate.parsers;

import io.jenkins.plugins.generate.PluginDataParser;
import io.jenkins.plugins.models.Maintainer;
import io.jenkins.plugins.models.Plugin;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class MaintainersPluginDataParser implements PluginDataParser {

  @Override
  public void parse(JSONObject pluginJson, Plugin plugin) {
    plugin.setMaintainers(StreamSupport.stream(pluginJson.optJSONArray("developers").spliterator(), false)
      .map(obj -> (JSONObject)obj)
      .map(developerJson -> {
        final String name = StringUtils.trimToNull(developerJson.optString("name", null));
        final String email = StringUtils.trimToNull(developerJson.optString("email", null));
        final String developerId = StringUtils.defaultString(
          StringUtils.trimToNull(developerJson.optString("developerId")),
          (name != null ? name : email)
        );
        return new Maintainer(
          developerId,
          name,
          email
        );
      }).collect(Collectors.toList()));
  }

}
