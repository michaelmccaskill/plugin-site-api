package io.jenkins.plugins.endpoints;

import io.jenkins.plugins.datastore.DatastoreException;
import io.jenkins.plugins.datastore.DatastoreService;
import io.jenkins.plugins.datastore.SortBy;
import io.jenkins.plugins.models.Plugins;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;

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
      @QueryParam("categories") List<String> categories,
      @QueryParam("labels") List<String> labels,
      @QueryParam("authors") List<String> authors,
      @DefaultValue("") @QueryParam("core")String core,
      @DefaultValue("50") @QueryParam("size") int size,
      @DefaultValue("1") @QueryParam("page") int page) {
    try {
      return datastoreService.search(query, sortBy, categories, labels, authors, core, size, page);
    } catch (DatastoreException e) {
      logger.error("Problem getting plugins", e);
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }
  }

  @Path("/downloaded")
  @GET
  public Plugins getMostDownloaded(@DefaultValue("10") @QueryParam("size") int size) {
    try {
      return datastoreService.search(null, SortBy.INSTALLS, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), null, size, 1);
    } catch (DatastoreException e) {
      logger.error("Problem getting most downloaded", e);
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }
  }

  @Path("/updated")
  @GET
  public Plugins getRecentlyUpdated(@DefaultValue("10") @QueryParam("size") int size) {
    try {
      return datastoreService.search(null, SortBy.UPDATED, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), null, size, 1);
    } catch (DatastoreException e) {
      logger.error("Problem getting recently updated", e);
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }
  }

}
