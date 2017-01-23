package io.jenkins.plugins.endpoints;

import io.jenkins.plugins.models.Info;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Properties;

/**
 * <p>Endpoint for retrieving information about the application</p>
 *
 * <p>Used for label filtering</p>
 */
@Path("/info")
@Produces(MediaType.APPLICATION_JSON)
public class InfoEndpoint {

  private Logger logger = LoggerFactory.getLogger(InfoEndpoint.class);

  private Info info = null;

  @GET
  public Info getLabels() {
    if (info != null) {
      return info;
    }
    try {
      final ClassLoader cl = getClass().getClassLoader();
      final Properties properties = new Properties();
      properties.load(cl.getResourceAsStream("git.properties"));
      this.info = new Info(properties.getProperty("git.commit.id"));
      return info;
    } catch (Exception e) {
      logger.error("Problem getting info", e);
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }
  }

}
