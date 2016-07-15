package io.jenkins.plugins.db.support;

import org.elasticsearch.client.Client;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.Immediate;

import javax.inject.Inject;
import javax.inject.Singleton;

@Immediate
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
