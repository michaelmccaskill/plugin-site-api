package io.jenkins.plugins.endpoints;

import io.jenkins.plugins.db.support.ElasticsearchTransformer;
import io.jenkins.plugins.schedule.JobScheduler;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.glassfish.hk2.api.Immediate;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/plugins")
@Produces(MediaType.APPLICATION_JSON)
@Immediate
public class Plugins {

  private final Logger logger = LoggerFactory.getLogger(Plugins.class);

  @Inject
  private Client esClient;

  // Hackity hack hack. This is only here because I can't figure out how to successfully
  // get the JobScheduler in RestApp from the ServiceLocator. Whenever I try I get NPE
  // so this injection at least makes @PostConstruct fire
  @Inject
  private JobScheduler jobScheduler;

  @GET
  public String search(
      @QueryParam("q") String query,
      @DefaultValue("name") @QueryParam("sort") String sort,
      @DefaultValue("") @QueryParam("labels") String labels,
      @DefaultValue("") @QueryParam("authors")String authors,
      @DefaultValue("") @QueryParam("core")String core,
      @DefaultValue("50") @QueryParam("size") int size,
      @DefaultValue("1") @QueryParam("page") int page) {
    try {
      final SearchRequestBuilder requestBuilder = esClient.prepareSearch("plugins")
        .addFields("name", "url", "title", "wiki", "excerpt", "labels")
        .addHighlightedField("excerpt")
        .setHighlighterFragmentSize(500)
        .setHighlighterNumOfFragments(1)
        .setHighlighterPreTags("<mark>")
        .setHighlighterPostTags("</mark>")
        .setFrom((page -1) * size)
        .setSize(size);
      final BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
        .should(QueryBuilders.matchQuery("title", query))
        .should(QueryBuilders.matchQuery("name", query))
        .must(QueryBuilders.matchQuery("excerpt", query));
      if (!labels.isEmpty()) {
        queryBuilder.should(QueryBuilders.termsQuery("labels", labels));
      }
      final SearchResponse response = requestBuilder.setQuery(queryBuilder).execute().get();
      final long total = response.getHits().getTotalHits();
      final JSONObject result = new JSONObject();
      result.put("docs", ElasticsearchTransformer.transformHits(response.getHits()));
      result.put("total", total);
      result.put("page", page);
      result.put("pages", (total + size - 1) / size);
      return result.toString(2);
    } catch (Exception e) {
      logger.error("Problem executing ES query", e);
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }
  }

}
