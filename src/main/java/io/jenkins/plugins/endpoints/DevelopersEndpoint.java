package io.jenkins.plugins.endpoints;

import io.jenkins.plugins.models.Developers;
import io.jenkins.plugins.services.DatastoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/developers")
@Produces(MediaType.APPLICATION_JSON)
public class DevelopersEndpoint {

  private Logger logger = LoggerFactory.getLogger(CategoriesEndpoint.class);

  @Inject
  private DatastoreService datastoreService;

  @GET
  public Developers getDevelopers() {
    try {
      return datastoreService.getDevelopers();
    } catch (Exception e) {
      logger.error("Problem getting developers", e);
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }
  }

}
