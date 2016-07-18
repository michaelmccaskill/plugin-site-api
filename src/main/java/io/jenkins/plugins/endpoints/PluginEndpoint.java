package io.jenkins.plugins.endpoints;

import io.jenkins.plugins.datastore.DatastoreException;
import io.jenkins.plugins.datastore.DatastoreService;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/plugin/{name}")
@Produces(MediaType.APPLICATION_JSON)
public class PluginEndpoint {

  private Logger logger = LoggerFactory.getLogger(PluginEndpoint.class);

  @Inject
  private DatastoreService datastoreService;

  @GET
  public String getPlugin(@PathParam("name") String name) {
    try {
      final JSONObject result = datastoreService.getPlugin(name);
      if (result != null) {
        return result.toString(2);
      } else {
        throw new WebApplicationException(Response.Status.NOT_FOUND);
      }
    } catch (DatastoreException e) {
      logger.error("Problem getting plugin " + name, e);
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }
  }

}
