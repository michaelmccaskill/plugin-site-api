package io.jenkins.plugins.endpoints;

import io.jenkins.plugins.services.DatastoreService;
import io.jenkins.plugins.models.Labels;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/labels")
@Produces(MediaType.APPLICATION_JSON)
public class LabelsEndpoint {

  private Logger logger = LoggerFactory.getLogger(LabelsEndpoint.class);

  @Inject
  private DatastoreService datastoreService;

  @GET
  public Labels getLabels() {
    try {
      return datastoreService.getLabels();
    } catch (Exception e) {
      logger.error("Problem getting labels", e);
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }
  }

}
