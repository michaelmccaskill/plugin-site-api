package io.jenkins.plugins.services.impl;

import io.jenkins.plugins.commons.JsonObjectMapper;
import io.jenkins.plugins.datastore.ElasticsearchTransformer;
import io.jenkins.plugins.models.*;
import io.jenkins.plugins.services.DatastoreService;
import io.jenkins.plugins.services.SearchOptions;
import io.jenkins.plugins.services.ServiceException;
import org.apache.commons.io.FileUtils;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.InternalNested;
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
  public Plugins search(SearchOptions searchOptions) throws ServiceException {
    try {
      final SearchRequestBuilder requestBuilder = esClient.prepareSearch("plugins")
        .setFrom((searchOptions.getPage() - 1) * searchOptions.getLimit())
        .setSize(searchOptions.getLimit());
      final BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
      if (searchOptions.getQuery() != null) {
        queryBuilder
          .should(QueryBuilders.matchQuery("title", searchOptions.getQuery()))
          .should(QueryBuilders.matchQuery("name", searchOptions.getQuery()))
          .must(QueryBuilders.matchQuery("excerpt", searchOptions.getQuery()));
      } else {
        queryBuilder.must(QueryBuilders.matchAllQuery());
      }
      if (!searchOptions.getAuthors().isEmpty() || !searchOptions.getCategories().isEmpty()
            || searchOptions.getCore() != null || !searchOptions.getLabels().isEmpty()) {
        final BoolQueryBuilder filter = QueryBuilders.boolQuery();
        if (!searchOptions.getCategories().isEmpty() && !searchOptions.getLabels().isEmpty()) {
          filter.must(
            QueryBuilders.boolQuery().should(
              QueryBuilders.termsQuery("categories", searchOptions.getCategories())
            ).should(
              QueryBuilders.termsQuery("labels", searchOptions.getLabels())
            )
          );
        } else if (!searchOptions.getCategories().isEmpty()) {
          filter.must(
            QueryBuilders.boolQuery().should(
              QueryBuilders.termsQuery("categories", searchOptions.getCategories())
            )
          );
        } else if (!searchOptions.getLabels().isEmpty()) {
          filter.must(
            QueryBuilders.boolQuery().should(
              QueryBuilders.termsQuery("labels", searchOptions.getLabels())
            )
          );
        }
        if (!searchOptions.getAuthors().isEmpty()) {
          filter.must(
            QueryBuilders.boolQuery().should(
              QueryBuilders.nestedQuery("developers", QueryBuilders.matchQuery("developers.name", searchOptions.getAuthors()))
            )
          );
        }
        if (searchOptions.getCore() != null) {
          filter.must(QueryBuilders.termQuery("requiredCore", searchOptions.getCore()));
        }
        queryBuilder.filter(filter);
      }
      requestBuilder.setQuery(queryBuilder);
      if (searchOptions.getSortBy() != null) {
        switch (searchOptions.getSortBy()) {
          case INSTALLS:
            requestBuilder.addSort(SortBuilders.fieldSort("stats.lifetime").setNestedPath("stats").order(SortOrder.DESC));
            break;
          case NAME:
            requestBuilder.addSort(SortBuilders.fieldSort("name.raw").order(SortOrder.ASC));
            break;
          case TITLE:
            requestBuilder.addSort(SortBuilders.fieldSort("title.raw").order(SortOrder.ASC));
            break;
          case UPDATED:
            requestBuilder.addSort(SortBuilders.fieldSort("releaseTimestamp").order(SortOrder.DESC));
            break;
          case RELEVANCE: break;
        }
      }
      final SearchResponse response = requestBuilder.execute().get();
      final long total = response.getHits().getTotalHits();
      final long pages = (total + searchOptions.getLimit() - 1) / searchOptions.getLimit();
      return new Plugins(
        ElasticsearchTransformer.transformHits(response.getHits()),
        searchOptions.getPage(), pages, total, searchOptions.getLimit()
      );
    } catch (Exception e) {
      logger.error("Problem executing, ES query", e);
      throw new ServiceException("Problem executing ES query", e);
    }
  }

  @Override
  public Plugin getPlugin(String name) throws ServiceException {
    try {
      final GetResponse getResponse = esClient.prepareGet("plugins", "plugins", name).execute().get();
      return getResponse.isExists() ? ElasticsearchTransformer.transformGet(getResponse, Plugin.class) : null;
    } catch (Exception e) {
        logger.error("Problem executing ES query", e);
        throw new ServiceException("Problem executing ES query", e);
    }
  }

  @Override
  public Categories getCategories() throws ServiceException {
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
      throw new ServiceException("Problem getting categories", e);
    }
  }

  @Override
  public Developers getDevelopers() throws ServiceException {
    try {
      final SearchRequestBuilder requestBuilder = esClient.prepareSearch("plugins")
        .addAggregation(AggregationBuilders.nested("developers").path("developers")
          .subAggregation(AggregationBuilders.terms("developers").field("developers.name.raw").size(0))
        )
        .setSize(0);
      final SearchResponse response = requestBuilder.execute().get();
      final List<String> developers = new ArrayList<>();
      final InternalNested nested = response.getAggregations().get("developers");
      final StringTerms agg = nested.getAggregations().get("developers");
      agg.getBuckets().forEach((entry) -> {
        developers.add(entry.getKeyAsString());
      });
      return new Developers(developers);
    } catch (Exception e) {
      logger.error("Problem getting developers", e);
      throw new ServiceException("Problem getting developers", e);
    }
  }

  @Override
  public Labels getLabels() throws ServiceException {
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
      throw new ServiceException("Problem getting labels", e);
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
