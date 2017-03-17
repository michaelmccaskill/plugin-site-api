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
import java.util.Set;

/**
 * <p>Endpoint for searching for plugins</p>
 */
@Path("/plugins")
@Produces(MediaType.APPLICATION_JSON)
public class PluginsEndpoint {

  private final Logger logger = LoggerFactory.getLogger(PluginsEndpoint.class);

  @Inject
  private DatastoreService datastoreService;

  /**
   * <p>Support searching for plugins via the <code>q</code> or query string, and filtering options</p>.
   *
   * @param query The query string used to search various plugin metadata
   * @param sortBy How the results should be sorted. Defaults to relevance
   * @param categories Filter matches by categories
   * @param labels Filter matches by labels
   * @param maintainers Filter matches by maintainers
   * @param core Filter matches by core Jenkins version
   * @param limit How many results to return per page
   * @param page What page of the result set to return
   * @return Matching plugins
   */
  @GET
  public Plugins search(
      @QueryParam("q") String query,
      @DefaultValue("relevance") @QueryParam("sort") SortBy sortBy,
      @QueryParam("categories") Set<String> categories,
      @QueryParam("labels") Set<String> labels,
      @QueryParam("maintainers") Set<String> maintainers,
      @QueryParam("core")String core,
      @DefaultValue("50") @QueryParam("limit") int limit,
      @DefaultValue("1") @QueryParam("page") int page) {
    try {
      return datastoreService.search(
        new SearchOptions.Builder()
          .withQuery(query)
          .withSortBy(sortBy)
          .withCategories(categories)
          .withLabels(labels)
          .withMaintainers(maintainers)
          .withCore(core)
          .withLimit(limit)
          .withPage(page)
          .build()
      );
    } catch (ServiceException e) {
      logger.error("Problem getting plugins", e);
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * <p>Return the latest installed plugins</p>
   *
   * @param limit The last "limit" plugins
   * @return Matching plugins
   */
  @Path("/installed")
  @GET
  public Plugins getMostInstalled(@DefaultValue("10") @QueryParam("limit") int limit) {
    try {
      return datastoreService.search(
        new SearchOptions.Builder()
          .withSortBy(SortBy.INSTALLED)
          .withLimit(limit)
          .build()
      );
    } catch (ServiceException e) {
      logger.error("Problem getting most installed", e);
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * <p>Return the newest plugins</p>
   *
   * <p>This is plugins created in the last 30 days</p>
   *
   * @param limit The last "limit" plugins
   * @return Matching plugins
   */
  @Path("/new")
  @GET
  public Plugins getNew(@DefaultValue("10") @QueryParam("limit") int limit) {
    try {
      return datastoreService.search(
        new SearchOptions.Builder()
          .withSortBy(SortBy.FIRST_RELEASE)
          .withLimit(limit)
          .build()
      );
    } catch (ServiceException e) {
      logger.error("Problem getting trend", e);
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * <p>Return the latest updated plugins</p>
   *
   * @param limit The last "limit" plugins
   * @return Matching plugins
   */
  @Path("/updated")
  @GET
  public Plugins getRecentlyUpdated(@DefaultValue("10") @QueryParam("limit") int limit) {
    try {
      return datastoreService.search(
        new SearchOptions.Builder()
          .withSortBy(SortBy.UPDATED)
          .withLimit(limit)
          .build()
      );
    } catch (ServiceException e) {
      logger.error("Problem getting recently updated", e);
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * <p>Return the latest trending plugins</p>
   *
   * @param limit The last "limit" plugins
   * @return Matching plugins
   */
  @Path("/trend")
  @GET
  public Plugins getTrend(@DefaultValue("10") @QueryParam("limit") int limit) {
    try {
      return datastoreService.search(
        new SearchOptions.Builder()
          .withSortBy(SortBy.TREND)
          .withLimit(limit)
          .build()
      );
    } catch (ServiceException e) {
      logger.error("Problem getting trend", e);
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }
  }

}
