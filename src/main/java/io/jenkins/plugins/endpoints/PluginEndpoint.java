package io.jenkins.plugins.endpoints;

import io.jenkins.plugins.services.ServiceException;
import io.jenkins.plugins.services.DatastoreService;
import io.jenkins.plugins.models.Plugin;
import io.jenkins.plugins.services.WikiService;
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

  @Inject
  private WikiService wikiService;

  @GET
  public Plugin getPlugin(@PathParam("name") String name) {
    try {
      final Plugin plugin = datastoreService.getPlugin(name);
      if (plugin != null) {
        final String rawContent = wikiService.getWikiContent(plugin);
        final String content = wikiService.cleanWikiContent(rawContent);
        plugin.getWiki().setContent(content);
        return plugin;
      } else {
        throw new WebApplicationException(Response.Status.NOT_FOUND);
      }
    } catch (ServiceException e) {
      logger.error("Problem getting plugin " + name, e);
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }
  }

}
