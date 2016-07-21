package io.jenkins.plugins.datastore.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jenkins.plugins.models.Plugin;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public class ElasticsearchTransformer {

  private static ObjectMapper objectMapper = new ObjectMapper();

  public static Plugin transformGet(GetResponse get) throws IOException {
    return objectMapper.readValue(get.getSourceAsString(), Plugin.class);
  }

  public static JSONArray transformHits(SearchHits hits) {
    final JSONArray json = new JSONArray();
    hits.forEach((hit) -> json.put(transformHit(hit)));
    return json;
  }

  public static JSONObject transformHit(SearchHit hit) {
    final JSONObject json = new JSONObject();
    hit.getFields().entrySet().stream().forEach((entry) -> {
      final String key = entry.getKey();
      if (key.equals("labels") || key.equals("categories")) {
        json.put(key, entry.getValue().getValues());
      } else {
        json.put(key, entry.getValue().getValues().get(0));
      }
    });
    if (hit.getHighlightFields().containsKey("excerpt")) {
      json.put("highlight", hit.getHighlightFields().get("excerpt").fragments()[0].string());
    }
    return json;
  }

}
