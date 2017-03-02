package io.jenkins.plugins.datastore;

import io.jenkins.plugins.commons.JsonObjectMapper;
import io.jenkins.plugins.models.Plugin;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * <p>Transforms various Elasticsearch structures into domain models</p>
 *
 * @see Plugin
 */
public class ElasticsearchTransformer {

  public static <T> T transformGet(GetResponse get, Class<T> type) throws IOException {
    return JsonObjectMapper.getObjectMapper().readValue(get.getSourceAsString(), type);
  }

  public static List<Plugin> transformHits(SearchHits hits) throws IOException {
    return StreamSupport.stream(hits.spliterator(), false)
      .map(ElasticsearchTransformer::transformHit)
      .filter(plugin -> plugin != null)
      .collect(Collectors.toList());
  }

  public static Plugin transformHit(SearchHit hit) {
    try {
      return JsonObjectMapper.getObjectMapper().readValue(hit.getSourceAsString(), Plugin.class);
    } catch (IOException e ) {
      return null;
    }
  }

}
