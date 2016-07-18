package io.jenkins.plugins.datastore.support;

import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ElasticsearchTransformer {

  private static Logger logger = LoggerFactory.getLogger(ElasticsearchTransformer.class);

  public static JSONObject transformGet(GetResponse get) {
    final JSONObject json = new JSONObject();
    get.getSource().entrySet().stream().forEach((entry) -> {
      json.put(entry.getKey(), entry.getValue());
    });
    return json;
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
