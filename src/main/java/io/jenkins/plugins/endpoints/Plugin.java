package io.jenkins.plugins.endpoints;

import io.jenkins.plugins.datastore.support.ElasticsearchTransformer;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.concurrent.ExecutionException;

@Path("/plugin/{name}")
@Produces(MediaType.APPLICATION_JSON)
public class Plugin {

  private final Logger logger = LoggerFactory.getLogger(Plugin.class);

  @Inject
  private Client esClient;

  @GET
  public String getPlugin(@PathParam("name") String name) {
    try {
      final GetResponse getResponse = esClient.prepareGet("plugins", "plugins", name).execute().get();
      if (getResponse.isExists()) {
        return ElasticsearchTransformer.transformGet(getResponse).toString(2);
      } else {
        throw new WebApplicationException(Response.Status.NOT_FOUND);
      }
    } catch (InterruptedException | ExecutionException e) {
      logger.error("Problem getting plugin " + name, e);
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }
  }

}
