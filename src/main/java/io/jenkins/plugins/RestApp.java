package io.jenkins.plugins;

import io.jenkins.plugins.db.support.ElasticsearchClientFactory;
import io.jenkins.plugins.db.support.EmbeddedElasticsearchServer;
import io.jenkins.plugins.schedule.JobScheduler;
import org.elasticsearch.client.Client;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.ApplicationPath;

@ApplicationPath("/")
public class RestApp extends ResourceConfig {

  @Inject
  public RestApp(ServiceLocator locator) {
    ServiceLocatorUtilities.enableImmediateScope(locator);

    register(new AbstractBinder() {
      @Override
      protected void configure() {
        bind(EmbeddedElasticsearchServer.class).to(EmbeddedElasticsearchServer.class).in(Singleton.class);
        bindFactory(ElasticsearchClientFactory.class).to(Client.class).in(Singleton.class);
        bind(JobScheduler.class).to(JobScheduler.class).in(Singleton.class);
      }
    });

    packages("io.jenkins.plugins");
  }

}
