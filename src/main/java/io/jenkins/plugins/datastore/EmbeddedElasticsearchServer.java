package io.jenkins.plugins.datastore;

import io.jenkins.plugins.commons.JsonObjectMapper;
import io.jenkins.plugins.models.GeneratedPluginData;
import io.jenkins.plugins.services.ConfigurationService;
import io.jenkins.plugins.utils.VersionUtils;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Copied and modified from:
 * https://orrsella.com/2014/10/28/embedded-elasticsearch-server-for-scala-integration-tests/
 */
public class EmbeddedElasticsearchServer {

  private final Logger logger = LoggerFactory.getLogger(EmbeddedElasticsearchServer.class);

  private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd_HH.mm.ss");
  private static final String ALIAS = "plugins";
  private static final String INDEX_PREFIX = "plugins_";
  private static final String TYPE = "plugins";

  private File tempDir;
  private Node node;

  public Client getClient() {
    return node.client();
  }

  @Inject
  private ConfigurationService configurationService;

  @Inject
  private ScheduledExecutorService scheduledExecutorService;

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
    populateIndex();
    scheduledExecutorService.scheduleWithFixedDelay(() -> populateIndex(), 12, 12, TimeUnit.HOURS);
    logger.info("Initializing elasticsearch done");
  }

  @PreDestroy
  public void preDestroy() {
    logger.info("Destroying elasticsearch");
    getClient().close();
    node.close();
    FileUtils.deleteQuietly(tempDir);
  }

  private void populateIndex() {
    try {
      final GeneratedPluginData data = configurationService.getIndexData();
      doPopulateIndex(data);
    } catch (Exception e) {
      logger.error("Problem populating index", e);
      throw new RuntimeException("Problem populating index", e);
    }
  }

  private void doPopulateIndex(GeneratedPluginData data) {
    final Optional<LocalDateTime> optCreatedAt = getCurrentCreatedAt();
    if (optCreatedAt.isPresent()) {
      final LocalDateTime createdAt = optCreatedAt.get();
      final LocalDateTime generatedCreatedAt = LocalDateTime.parse(TIMESTAMP_FORMATTER.format(data.getCreatedAt()), TIMESTAMP_FORMATTER);
      logger.info("Current timestamp - " + createdAt);
      logger.info("Data timestamp    - " + generatedCreatedAt);
      if (createdAt.equals(generatedCreatedAt) || createdAt.isAfter(generatedCreatedAt)) {
        logger.info("Plugin data is already up to date");
        return;
      }
    }
    final String mappingVersion = VersionUtils.getMappingVersion();
    if (data.getMappingVersion() != null && !data.getMappingVersion().equalsIgnoreCase(mappingVersion)) {
      logger.warn(String.format("Data has mapping version '%s' but application has '%s'", data.getMappingVersion(), mappingVersion));
      logger.warn("Cannot index with new data. More than likely the application needs to be rebuilt and deployed first");
      return;
    }
    final String elasticsearchVersion = VersionUtils.getElasticsearchVersion();
    if (data.getElasticsearchVersion() != null && !data.getElasticsearchVersion().equalsIgnoreCase(elasticsearchVersion)) {
      logger.warn(String.format("Data has Elasticsearch version '%s' but application has '%s'", data.getElasticsearchVersion(), elasticsearchVersion));
      logger.warn("Cannot index with new data. More than likely the application needs to be rebuilt and deployed first");
      return;
    }
    final ClassLoader cl = getClass().getClassLoader();
    final String index = String.format("%s%s", INDEX_PREFIX, TIMESTAMP_FORMATTER.format(data.getCreatedAt()));
    try {
      final File mappingFile = new File(cl.getResource("elasticsearch/mappings/plugins.json").getFile());
      final String mappingContent = FileUtils.readFileToString(mappingFile, StandardCharsets.UTF_8);
      final Client client = getClient();
      client.admin().indices().prepareCreate(index)
        .addMapping(TYPE, mappingContent)
        .get();
      logger.info(String.format("Index '%s' created", index));
      final BulkRequestBuilder bulkRequestBuilder = client.prepareBulk();
      data.getPlugins().forEach((plugin) -> {
        try {
          final IndexRequest indexRequest = client.prepareIndex(index, TYPE, plugin.getName())
            .setSource(JsonObjectMapper.getObjectMapper().writeValueAsString(plugin)).request();
          bulkRequestBuilder.add(indexRequest);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      });
      final BulkResponse response = bulkRequestBuilder.get();
      if (response.hasFailures()) {
        for (BulkItemResponse item : response.getItems()) {
          logger.warn(String.format("Problem indexing: %s", item.getFailureMessage()));
        }
        throw new ElasticsearchException("Problem bulk indexing");
      }
      logger.info(String.format("Indexed %d plugins", data.getPlugins().size()));
      if (client.admin().indices().prepareAliasesExist(ALIAS).get().exists()) {
        final String oldIndex = client.admin().indices().prepareGetAliases(ALIAS).get().getAliases().iterator().next().key;
        // Atomic swap of alias
        client.admin().indices().prepareAliases()
          .removeAlias(oldIndex, ALIAS)
          .addAlias(index, ALIAS)
          .get();
        logger.info(String.format("Updated alias '%s' from '%s' to '%s'", ALIAS, oldIndex, index));
        client.admin().indices().prepareDelete(oldIndex).get();
        logger.info(String.format("Deleted old index '%s'", oldIndex));
      } else {
        client.admin().indices().prepareAliases()
          .addAlias(index, ALIAS)
          .get();
        logger.info(String.format("Alias (%s) plugins points to index %s", ALIAS, index));
      }
      client.admin().indices().prepareRefresh(ALIAS).execute().get();
    } catch (Exception e) {
      logger.error("Problem indexing", e);
      throw new RuntimeException("Problem indexing", e);
    }
  }

  private Optional<LocalDateTime> getCurrentCreatedAt() {
    final Client client = getClient();
    if (client.admin().indices().prepareAliasesExist(ALIAS).get().exists()) {
      final String index = client.admin().indices().prepareGetAliases(ALIAS).get().getAliases().iterator().next().key;
      final String timestamp = index.substring(INDEX_PREFIX.length());
      try {
        return Optional.of(LocalDateTime.parse(timestamp, TIMESTAMP_FORMATTER));
      } catch (Exception e) {
        logger.error("Problem parsing timestamp from index", e);
        return Optional.empty();
      }
    } else {
      logger.info("Alias doesn't exist");
      return Optional.empty();
    }
  }

}
