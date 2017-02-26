package io.jenkins.plugins.datastore;

import org.apache.commons.io.FileUtils;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Copied and modified from:
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
    logger.info("Initialized elasticsearch");
  }

  @PreDestroy
  public void preDestroy() {
    logger.info("Destroying elasticsearch");
    node.client().close();
    node.close();
    FileUtils.deleteQuietly(tempDir);
    logger.info("Destroyed elasticsearch");
  }

}
