package io.jenkins.plugins.endpoints;

import io.jenkins.plugins.services.PrepareDatastoreService;
import io.jenkins.plugins.utils.VersionUtils;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * <p>Endpoint for retrieving health about the application</p>
 */
@Path("/health")
@Produces(MediaType.APPLICATION_JSON)
public class HealthEndpoint {

  private Logger logger = LoggerFactory.getLogger(HealthEndpoint.class);

  @Inject
  private PrepareDatastoreService prepareDatastoreService;

  @Path("/elasticsearch")
  @GET
  public Map<String, Object> getElasticsearchHealth() {
    final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    final Map<String, Object> result = new HashMap<>();
    final LocalDateTime createdAt = prepareDatastoreService.getCurrentCreatedAt();
    result.put("createdAt", createdAt != null ? formatter.format(createdAt) : null);
    result.put("mappingVesion", VersionUtils.getMappingVersion());
    result.put("elasticsearchVesion", VersionUtils.getElasticsearchVersion());
    return result;
  }

}
