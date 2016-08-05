package io.jenkins.plugins.datastore;

import io.jenkins.plugins.commons.JsonObjectMapper;
import io.jenkins.plugins.models.Plugin;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ElasticsearchTransformer {

  public static <T> T transformGet(GetResponse get, Class<T> type) throws IOException {
    return JsonObjectMapper.getObjectMapper().readValue(get.getSourceAsString(), type);
  }

  public static List<Plugin> transformHits(SearchHits hits) throws IOException {
    final List<Plugin> results = new ArrayList<>();
    for (SearchHit hit : hits) {
      results.add(transformHit(hit));
    }
    return results;
  }

  public static Plugin transformHit(SearchHit hit) throws IOException {
    return JsonObjectMapper.getObjectMapper().readValue(hit.getSourceAsString(), Plugin.class);
  }

}
