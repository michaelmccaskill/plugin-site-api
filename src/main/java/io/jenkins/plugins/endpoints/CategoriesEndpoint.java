package io.jenkins.plugins.endpoints;

import io.jenkins.plugins.datastore.DatastoreService;
import io.jenkins.plugins.models.Category;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.util.List;

@Path("/categories")
@Produces(MediaType.APPLICATION_JSON)
public class CategoriesEndpoint {

  private Logger logger = LoggerFactory.getLogger(CategoriesEndpoint.class);

  @Inject
  private DatastoreService datastoreService;

  @GET
  public String getCategories() {
    try {
      final List<Category> categories = datastoreService.getCategories();
      final JSONObject result = new JSONObject();
      result.put("categories", categories);
      return result.toString(2);
    } catch (Exception e) {
      logger.error("Problem getting categories", e);
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }
  }

}
