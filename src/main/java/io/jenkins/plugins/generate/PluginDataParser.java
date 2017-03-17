package io.jenkins.plugins.generate;

import io.jenkins.plugins.models.Plugin;
import org.json.JSONObject;

/**
 * <p>Contract for parsing pieces of <code>pluginJson</code> and applying them to <code>plugin</code></p>
 */
public interface PluginDataParser {

  /**
   * <p>Parse relevant pieces of <code>pluginJson</code> and apply to <code>plugin</code></p>
   *
   * @param pluginJson JSON representing a plugin
   * @param plugin Partial plugin to apply <code>pluginJson</code>
     */
  void parse(JSONObject pluginJson, Plugin plugin);

}
