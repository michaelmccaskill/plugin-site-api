package io.jenkins.plugins.datastore.support;

import io.jenkins.plugins.commons.JsonObjectMapper;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public class ElasticsearchTransformer {

  public static <T> T transformGet(GetResponse get, Class<T> type) throws IOException {
    return JsonObjectMapper.getObjectMapper().readValue(get.getSourceAsString(), type);
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
