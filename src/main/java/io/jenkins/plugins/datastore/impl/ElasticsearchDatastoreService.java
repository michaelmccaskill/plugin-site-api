package io.jenkins.plugins.datastore.impl;

import io.jenkins.plugins.datastore.DatastoreException;
import io.jenkins.plugins.datastore.DatastoreService;
import io.jenkins.plugins.datastore.support.ElasticsearchTransformer;
import io.jenkins.plugins.models.Plugin;
import org.apache.commons.io.FileUtils;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ElasticsearchDatastoreService implements DatastoreService {

  private Logger logger = LoggerFactory.getLogger(ElasticsearchDatastoreService.class);

  @Inject
  private Client esClient;

  @Override
  public JSONObject search(String query, String sort, List<String> labels, List<String> authors, String core, Integer size, Integer page) throws DatastoreException {
    try {
      final SearchRequestBuilder requestBuilder = esClient.prepareSearch("plugins")
        .addFields("name", "url", "title", "wiki", "excerpt", "labels", "categories")
        .addHighlightedField("excerpt")
        .setHighlighterFragmentSize(500)
        .setHighlighterNumOfFragments(1)
        .setHighlighterPreTags("<mark>")
        .setHighlighterPostTags("</mark>")
        .setFrom((page - 1) * size)
        .setSize(size);
      if (query != null && !query.trim().isEmpty()) {
        final BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
          .should(QueryBuilders.matchQuery("title", query))
          .should(QueryBuilders.matchQuery("name", query))
          .must(QueryBuilders.matchQuery("excerpt", query));
        if (!labels.isEmpty()) {
          queryBuilder.should(QueryBuilders.termsQuery("labels", labels));
        }
        requestBuilder.setQuery(queryBuilder);
      } else {
        requestBuilder.setQuery(QueryBuilders.matchAllQuery());
      }
      final SearchResponse response = requestBuilder.execute().get();
      final long total = response.getHits().getTotalHits();
      final JSONObject result = new JSONObject();
      result.put("docs", ElasticsearchTransformer.transformHits(response.getHits()));
      result.put("total", total);
      result.put("page", page);
      result.put("pages", (total + size - 1) / size);
      return result;
    } catch (InterruptedException | ExecutionException e) {
      logger.error("Problem executing, ES query", e);
      throw new DatastoreException("Problem executing ES query", e);
    }
  }

  @Override
  public Plugin getPlugin(String name) {
    try {
      final GetResponse getResponse = esClient.prepareGet("plugins", "plugins", name).execute().get();
      return getResponse.isExists() ? ElasticsearchTransformer.transformGet(getResponse) : null;
    } catch (Exception e) {
        throw new DatastoreException("Problem executing ES query", e);
    }
  }

  @Override
  public JSONObject getCategories() throws DatastoreException {
    try {
      final ClassLoader cl = getClass().getClassLoader();
      final File file = new File(cl.getResource("categories.json").getFile());
      final String json = FileUtils.readFileToString(file, "utf-8");
      return new JSONObject(json);
    } catch (Exception e) {
      logger.error("Problem getting categories", e);
      throw new DatastoreException("Problem getting categories", e);
    }
  }

  @Override
  public JSONObject getLabels() throws DatastoreException {
    try {
      final SearchRequestBuilder requestBuilder = esClient.prepareSearch("plugins")
        .addAggregation(AggregationBuilders.terms("labels").field("labels").size(0))
        .setSize(0);
      final SearchResponse response = requestBuilder.execute().get();
      final JSONArray docs = new JSONArray();
      final StringTerms agg = response.getAggregations().get("labels");
      agg.getBuckets().forEach((entry) -> {
        final String key = entry.getKey();
        final long value = entry.getDocCount();
        final JSONObject result = new JSONObject();
        result.put("key" , key);
        result.put("value", value);
        docs.put(result);
      });
      final JSONObject result = new JSONObject();
      result.put("docs", docs);
      return result;
    } catch (InterruptedException | ExecutionException e) {
      logger.error("Problem getting labels", e);
      throw new DatastoreException("Problem getting labels", e);
    }
  }
}
