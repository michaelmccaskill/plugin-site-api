package io.jenkins.plugins.datastore.impl;

import io.jenkins.plugins.commons.JsonObjectMapper;
import io.jenkins.plugins.datastore.DatastoreException;
import io.jenkins.plugins.datastore.DatastoreService;
import io.jenkins.plugins.datastore.SortBy;
import io.jenkins.plugins.datastore.support.ElasticsearchTransformer;
import io.jenkins.plugins.models.*;
import org.apache.commons.io.FileUtils;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ElasticsearchDatastoreService implements DatastoreService {

  private Logger logger = LoggerFactory.getLogger(ElasticsearchDatastoreService.class);

  @Inject
  private Client esClient;

  @Override
  public Plugins search(String query, SortBy sortBy, List<String> labels, List<String> authors, String core, Integer size, Integer page) throws DatastoreException {
    try {
      final SearchRequestBuilder requestBuilder = esClient.prepareSearch("plugins")
        .setFrom((page - 1) * size)
        .setSize(size);
      final BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
      if (query != null && !query.trim().isEmpty()) {
        queryBuilder
          .should(QueryBuilders.matchQuery("title", query))
          .should(QueryBuilders.matchQuery("name", query))
          .must(QueryBuilders.matchQuery("excerpt", query));
      } else {
        queryBuilder.must(QueryBuilders.matchAllQuery());
      }
      if (!labels.isEmpty()) {
        queryBuilder.must(QueryBuilders.termsQuery("labels", labels));
      }
      if (!authors.isEmpty()) {
        queryBuilder.must(QueryBuilders.nestedQuery("developers", QueryBuilders.matchQuery("developers.name", authors)));
      }
      if (core != null && !core.trim().isEmpty()) {
        queryBuilder.must(QueryBuilders.termQuery("requiredCore", core));
      }
      requestBuilder.setQuery(queryBuilder);
      if (sortBy != null) {
        switch (sortBy) {
          case INSTALLS:
            requestBuilder.addSort(SortBuilders.fieldSort("stats.lifetime").setNestedPath("stats").order(SortOrder.DESC));
            break;
          case NAME:
            requestBuilder.addSort(SortBuilders.fieldSort("name").order(SortOrder.ASC));
            break;
          case UPDATED:
            requestBuilder.addSort(SortBuilders.fieldSort("buildDate").order(SortOrder.DESC));
            break;
          case RELEVANCE: break;
        }
      }
      final SearchResponse response = requestBuilder.execute().get();
      final long total = response.getHits().getTotalHits();
      final long pages = (total + size - 1) / size;
      final Plugins result = new Plugins();
      result.setPlugins(ElasticsearchTransformer.transformHits(response.getHits()));
      result.setTotal(total);
      result.setPage(page);
      result.setPages(pages);
      return result;
    } catch (Exception e) {
      logger.error("Problem executing, ES query", e);
      throw new DatastoreException("Problem executing ES query", e);
    }
  }

  @Override
  public Plugin getPlugin(String name) throws DatastoreException {
    try {
      final GetResponse getResponse = esClient.prepareGet("plugins", "plugins", name).execute().get();
      return getResponse.isExists() ? ElasticsearchTransformer.transformGet(getResponse, Plugin.class) : null;
    } catch (Exception e) {
        logger.error("Problem executing ES query", e);
        throw new DatastoreException("Problem executing ES query", e);
    }
  }

  @Override
  public Categories getCategories() throws DatastoreException {
    try {
      final ClassLoader cl = getClass().getClassLoader();
      final File file = new File(cl.getResource("categories.json").getFile());
      final JSONArray json = new JSONObject(FileUtils.readFileToString(file, "utf-8")).getJSONArray("categories");
      final List<Category> categories = new ArrayList<>();
      for (Object entry : json) {
        final Category category = JsonObjectMapper.getObjectMapper().readValue(entry.toString(), Category.class);
        categories.add(category);
      }
      return new Categories(categories);
    } catch (Exception e) {
      logger.error("Problem getting categories", e);
      throw new DatastoreException("Problem getting categories", e);
    }
  }

  @Override
  public Labels getLabels() throws DatastoreException {
    try {
      final Map<String, String> labelTitleMap = buildLabelTitleMap();
      final SearchRequestBuilder requestBuilder = esClient.prepareSearch("plugins")
        .addAggregation(AggregationBuilders.terms("labels").field("labels").size(0))
        .setSize(0);
      final SearchResponse response = requestBuilder.execute().get();
      final List<Label> labels = new ArrayList<>();
      final StringTerms agg = response.getAggregations().get("labels");
      agg.getBuckets().forEach((entry) -> {
        final String key = entry.getKeyAsString();
        final Label label = new Label(
          key, labelTitleMap.getOrDefault(key, null)
        );
        labels.add(label);
      });
      return new Labels(labels);
    } catch (Exception e) {
      logger.error("Problem getting labels", e);
      throw new DatastoreException("Problem getting labels", e);
    }
  }

  private Map<String, String> buildLabelTitleMap() {
    try {
      final ClassLoader cl = getClass().getClassLoader();
      final File file = new File(cl.getResource("labels.json").getFile());
      final JSONArray labels = new JSONObject(FileUtils.readFileToString(file, "utf-8")).getJSONArray("labels");
      final Map<String, String> result = new HashMap<>();
      for (int i = 0; i < labels.length(); i++) {
        final JSONObject label = labels.getJSONObject(i);
        result.put(label.getString("id"), label.getString("title"));
      }
      return result;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
