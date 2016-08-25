package io.jenkins.plugins.endpoints;

import io.jenkins.plugins.models.Plugins;
import io.jenkins.plugins.services.DatastoreService;
import io.jenkins.plugins.services.SearchOptions;
import io.jenkins.plugins.services.ServiceException;
import io.jenkins.plugins.services.SortBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collections;

@Path("/plugins")
@Produces(MediaType.APPLICATION_JSON)
public class PluginsEndpoint {

  private final Logger logger = LoggerFactory.getLogger(PluginsEndpoint.class);

  @Inject
  private DatastoreService datastoreService;

  @GET
  public Plugins search(
      @QueryParam("q") String query,
      @DefaultValue("relevance") @QueryParam("sort") SortBy sortBy,
      @DefaultValue("") @QueryParam("categories") String categories,
      @DefaultValue("") @QueryParam("labels") String labels,
      @DefaultValue("") @QueryParam("maintainers") String maintainers,
      @DefaultValue("") @QueryParam("core")String core,
      @DefaultValue("50") @QueryParam("limit") int limit,
      @DefaultValue("1") @QueryParam("page") int page) {
    try {
      return datastoreService.search(new SearchOptions(query, sortBy, categories, labels, maintainers, core, limit, page));
    } catch (ServiceException e) {
      logger.error("Problem getting plugins", e);
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }
  }

  @Path("/installed")
  @GET
  public Plugins getMostInstalled(@DefaultValue("10") @QueryParam("limit") int limit) {
    try {
      return datastoreService.search(new SearchOptions(null, SortBy.INSTALLED, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), null, limit, 1));
    } catch (ServiceException e) {
      logger.error("Problem getting most installed", e);
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }
  }

  @Path("/updated")
  @GET
  public Plugins getRecentlyUpdated(@DefaultValue("10") @QueryParam("limit") int limit) {
    try {
      return datastoreService.search(new SearchOptions(null, SortBy.UPDATED, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), null, limit, 1));
    } catch (ServiceException e) {
      logger.error("Problem getting recently updated", e);
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }
  }

  @Path("/trend")
  @GET
  public Plugins getTrend(@DefaultValue("10") @QueryParam("limit") int limit) {
    try {
      return datastoreService.search(new SearchOptions(null, SortBy.TREND, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), null, limit, 1));
    } catch (ServiceException e) {
      logger.error("Problem getting trend", e);
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }
  }

}
