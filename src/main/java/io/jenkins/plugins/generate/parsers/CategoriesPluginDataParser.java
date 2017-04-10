package io.jenkins.plugins.generate.parsers;

import io.jenkins.plugins.generate.PluginDataParser;
import io.jenkins.plugins.models.Plugin;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class CategoriesPluginDataParser implements PluginDataParser {

  private final Map<String, String> labelToCategoryMap;

  public CategoriesPluginDataParser() {
    labelToCategoryMap = buildLabelToCategoryMap();
  }

  @Override
  public void parse(JSONObject pluginJson, Plugin plugin) {
    plugin.setCategories(plugin.getLabels().stream()
      .filter(labelToCategoryMap::containsKey)
      .map(labelToCategoryMap::get)
      .collect(Collectors.toSet()));
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

}
