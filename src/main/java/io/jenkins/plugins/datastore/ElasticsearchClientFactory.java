package io.jenkins.plugins.datastore;

import org.elasticsearch.client.Client;
import org.glassfish.hk2.api.Factory;

import javax.inject.Inject;

/**
 * <p><code>Factory</code> for the Elasticsearch <code>Client</code></p>
 *
 * <p>This enables the ability to inject <code>Client</code> into the service tier without having to involve the
 * <code>EmbeddedElasticsearchServer</code></p>
 */
public class ElasticsearchClientFactory implements Factory<Client> {

  private final EmbeddedElasticsearchServer server;

  @Inject
  public ElasticsearchClientFactory(EmbeddedElasticsearchServer server) {
    this.server = server;
  }

  @Override
  public Client provide() {
    return server.getClient();
  }

  @Override
  public void dispose(Client client) {
  }

}
