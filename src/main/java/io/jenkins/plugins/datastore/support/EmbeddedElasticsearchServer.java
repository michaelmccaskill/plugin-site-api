package io.jenkins.plugins.datastore.support;

import org.apache.commons.io.FileUtils;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.glassfish.hk2.api.Immediate;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Copied & modified from:
 * https://orrsella.com/2014/10/28/embedded-elasticsearch-server-for-scala-integration-tests/
 */
@Service
@Immediate
public class EmbeddedElasticsearchServer {

  private final Logger logger = LoggerFactory.getLogger(EmbeddedElasticsearchServer.class);

  private File dataDir;
  private Node node;

  public Client getClient() {
    return node.client();
  }

  @PostConstruct
  public void postConstruct() {
    logger.info("Initialize elasticsearch");
    try {
      this.dataDir = Files.createTempDirectory("elasticsearch_data").toFile();
    } catch (IOException e) {
      logger.error("Problem creating temp data directory", e);
      throw new RuntimeException(e);
    }
    final Settings settings = ImmutableSettings.settingsBuilder()
      .put("path.data", dataDir)
      .put("http.enabled", "false")
      .build();
    node = NodeBuilder.nodeBuilder().local(true).settings(settings).build();
    node.start();
    createIndex();
    logger.info("Initializing elasticsearch done");
  }

  @PreDestroy
  public void preDestroy() {
    node.close();
    FileUtils.deleteQuietly(this.dataDir);
  }

  public void createIndex() {
    try {
      final ClassLoader cl = getClass().getClassLoader();
      final File file = new File(cl.getResource("elasticsearch/plugins.json").getFile());
      final String body = FileUtils.readFileToString(file, "utf-8");
      final Client client = getClient();
      client.admin().indices().prepareCreate("plugins")
        .addMapping("plugins", body)
        .get();
    } catch (Exception e) {
      logger.error("Problem creating index", e);
    }

  }

}
