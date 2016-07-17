package io.jenkins.plugins.datastore;

import io.jenkins.plugins.datastore.impl.ElasticsearchDatastoreService;
import io.jenkins.plugins.datastore.support.ElasticsearchClientFactory;
import io.jenkins.plugins.datastore.support.EmbeddedElasticsearchServer;
import org.elasticsearch.client.Client;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import javax.inject.Singleton;

public class Binder extends AbstractBinder {

  @Override
  protected void configure() {
    bind(EmbeddedElasticsearchServer.class).to(EmbeddedElasticsearchServer.class).in(Singleton.class);
    bindFactory(ElasticsearchClientFactory.class).to(Client.class).in(Singleton.class);
    bind(ElasticsearchDatastoreService.class).to(DatastoreService.class).in(Singleton.class);
  }
}
