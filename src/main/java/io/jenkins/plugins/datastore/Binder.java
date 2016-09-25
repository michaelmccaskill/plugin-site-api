package io.jenkins.plugins.datastore;

import org.elasticsearch.client.Client;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import javax.inject.Singleton;

/**
 * <p>Binder for the data tier</p>
 *
 * <p>Binds</p>
 * <ul>
 *   <li><code>EmbeddedElasticsearchServer</code> to itself as a <code>Singleton</code></li>
 *   <li><code>ElasticsearchClientFactory</code> to <code>Client</code> as a <code>Singleton</code></li>
 * </ul>
 *
 * @see ElasticsearchClientFactory
 * @see EmbeddedElasticsearchServer
 */
public class Binder extends AbstractBinder {

  @Override
  protected void configure() {
    bind(EmbeddedElasticsearchServer.class).to(EmbeddedElasticsearchServer.class).in(Singleton.class);
    bindFactory(ElasticsearchClientFactory.class).to(Client.class).in(Singleton.class);
  }
}
