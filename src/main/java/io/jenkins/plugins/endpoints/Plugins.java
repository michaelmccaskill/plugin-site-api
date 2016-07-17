package io.jenkins.plugins.endpoints;

import io.jenkins.plugins.schedule.JobScheduler;
import io.jenkins.plugins.service.SearchService;
import io.jenkins.plugins.service.ServiceException;
import org.glassfish.hk2.api.Immediate;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/plugins")
@Produces(MediaType.APPLICATION_JSON)
@Immediate
public class Plugins {

  private final Logger logger = LoggerFactory.getLogger(Plugins.class);

  @Inject
  private SearchService searchService;

  // Hackity hack hack. This is only here because I can't figure out how to successfully
  // get the JobScheduler in RestApp from the ServiceLocator. Whenever I try I get NPE
  // so this injection at least makes @PostConstruct fire
  @Inject
  private JobScheduler jobScheduler;

  @GET
  public String search(
      @QueryParam("q") String query,
      @DefaultValue("name") @QueryParam("sort") String sort,
      @QueryParam("labels") List<String> labels,
      @QueryParam("authors") List<String> authors,
      @DefaultValue("") @QueryParam("core")String core,
      @DefaultValue("50") @QueryParam("size") int size,
      @DefaultValue("1") @QueryParam("page") int page) {
    try {
      final JSONObject result = searchService.search(query, sort, labels, authors, core, size, page);
      return result.toString(2);
    } catch (ServiceException e) {
      logger.error("Problem executing ES query", e);
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }
  }

}
