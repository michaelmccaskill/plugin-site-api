package io.jenkins.plugins.endpoints;

import io.jenkins.plugins.datastore.DatastoreException;
import io.jenkins.plugins.datastore.DatastoreService;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/plugin/{name}")
@Produces(MediaType.APPLICATION_JSON)
public class PluginEndpoint {

  @Inject
  private DatastoreService datastoreService;

  @GET
  public String getPlugin(@PathParam("name") String name) {
    try {
      final JSONObject result = datastoreService.get(name);
      if (result != null) {
        return result.toString(2);
      } else {
        throw new WebApplicationException(Response.Status.NOT_FOUND);
      }
    } catch (DatastoreException e) {
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }
  }

}
