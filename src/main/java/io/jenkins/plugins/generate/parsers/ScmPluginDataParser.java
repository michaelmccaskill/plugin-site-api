package io.jenkins.plugins.generate.parsers;

import io.jenkins.plugins.generate.PluginDataParser;
import io.jenkins.plugins.models.Plugin;
import io.jenkins.plugins.models.Scm;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

public class ScmPluginDataParser implements PluginDataParser {

  @Override
  public void parse(JSONObject pluginJson, Plugin plugin) {
    final String scmString = StringUtils.trimToNull(pluginJson.optString("scm", null));
    final Scm scm = new Scm();
    scm.setLink(scmString);
    plugin.setScm(scm);
  }
}
