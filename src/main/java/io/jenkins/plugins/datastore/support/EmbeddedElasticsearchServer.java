package io.jenkins.plugins.datastore.support;

import org.apache.commons.io.FileUtils;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Copied & modified from:
 * https://orrsella.com/2014/10/28/embedded-elasticsearch-server-for-scala-integration-tests/
 */
public class EmbeddedElasticsearchServer {

  private final Logger logger = LoggerFactory.getLogger(EmbeddedElasticsearchServer.class);

  private File tempDir;
  private Node node;

  public Client getClient() {
    return node.client();
  }

  @PostConstruct
  public void postConstruct() {
    logger.info("Initialize elasticsearch");
    try {
      tempDir = Files.createTempDirectory("elasticsearch_").toFile();
    } catch (IOException e) {
      logger.error("Problem creating temp data directory", e);
      throw new RuntimeException(e);
    }
    final Settings settings = Settings.settingsBuilder()
      .put("path.home", tempDir)
      .put("http.enabled", "false")
      .build();
    node = NodeBuilder.nodeBuilder().local(true).settings(settings).build();
    node.start();
    createAndPopulateIndex();
    logger.info("Initializing elasticsearch done");
  }

  @PreDestroy
  public void preDestroy() {
    logger.info("Destroying elasticsearch");
    getClient().close();
    node.close();
    FileUtils.deleteQuietly(tempDir);
  }

  private void createAndPopulateIndex() {
    final ClassLoader cl = getClass().getClassLoader();
    final String index = String.format("plugins_%s", DateTimeFormatter.ofPattern("yyyy.mm.dd_HH.mm.ss").format(LocalDateTime.now()));
    try {
      final File mappingFile = new File(cl.getResource("elasticsearch/mappings/plugins.json").getFile());
      final String mappingContent = FileUtils.readFileToString(mappingFile, "utf-8");
      final Client client = getClient();
      client.admin().indices().prepareCreate(index)
        .addMapping("plugins", mappingContent)
        .get();
      logger.info(String.format("Index '%s' created", index));
      final File dataFile = new File(cl.getResource("elasticsearch/data/plugins.json").getFile());
      final String data = FileUtils.readFileToString(dataFile, "utf-8");
      final JSONObject json = new JSONObject(data);
      final BulkRequestBuilder bulkRequestBuilder = client.prepareBulk();
      json.keySet().stream().forEach((key) -> {
        final JSONObject plugin = json.getJSONObject(key);
        final IndexRequest indexRequest = client.prepareIndex(index, "plugins", key).setSource(plugin.toString()).request();
        bulkRequestBuilder.add(indexRequest);
      });
      final BulkResponse response = bulkRequestBuilder.get();
      if (response.hasFailures()) {
        for (BulkItemResponse item : response.getItems()) {
          logger.warn("Problem indexing: " + item.getFailureMessage());
        }
        throw new ElasticsearchException("Problem bulk indexing");
      }
      logger.info(String.format("Indexed %d plugins", json.keySet().size()));
      client.admin().indices().prepareAliases().addAlias(index, "plugins").get();
      client.admin().indices().prepareRefresh("plugins").execute().get();
      logger.info(String.format("Alias plugins points to index %s", index));
    } catch (Exception e) {
      logger.error("Problem creating and populating index", e);
      throw new RuntimeException("Problem creating and populating index", e);
    }
  }

}
