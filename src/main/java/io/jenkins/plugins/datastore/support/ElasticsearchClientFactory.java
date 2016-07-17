package io.jenkins.plugins.datastore.support;

import org.elasticsearch.client.Client;
import org.glassfish.hk2.api.Factory;

import javax.inject.Inject;

public class ElasticsearchClientFactory implements Factory<Client> {

  @Inject
  private EmbeddedElasticsearchServer es;

  @Override
  public Client provide() {
    return es.getClient();
  }

  @Override
  public void dispose(Client client) {
  }

}
