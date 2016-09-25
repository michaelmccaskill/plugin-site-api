package io.jenkins.plugins.services;

import io.jenkins.plugins.services.impl.ElasticsearchDatastoreService;
import io.jenkins.plugins.services.impl.HttpClientWikiService;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import javax.inject.Singleton;

/**
 * <p>Binder for the service tier</p>
 *
 * <p>Binds</p>
 * <ul>
 *   <li><code>ElasticsearchDatastoreService</code> to <code>DatastoreService</code> as a <code>Singleton</code></li>
 *   <li><code>HttpClientWikiService</code> to <code>WikiService</code> as a <code>Singleton</code></li>
 * </ul>
 *
 * @see ElasticsearchDatastoreService
 * @see HttpClientWikiService
 */
public class Binder extends AbstractBinder {

  @Override
  protected void configure() {
    bind(ElasticsearchDatastoreService.class).to(DatastoreService.class).in(Singleton.class);
    bind(HttpClientWikiService.class).to(WikiService.class).in(Singleton.class);
  }
}
