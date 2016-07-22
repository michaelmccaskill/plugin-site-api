package io.jenkins.plugins;

import io.jenkins.plugins.datastore.support.EmbeddedElasticsearchServer;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spi.Container;
import org.glassfish.jersey.server.spi.ContainerLifecycleListener;

import javax.ws.rs.ApplicationPath;

@ApplicationPath("/")
public class RestApp extends ResourceConfig {

  public RestApp() {

    register(new io.jenkins.plugins.datastore.Binder());

    register(new ContainerLifecycleListener() {
      @Override
      public void onStartup(Container container) {
        final ServiceLocator locator = container.getApplicationHandler().getServiceLocator();
        // Preload so ES gets indexed
        locator.getService(EmbeddedElasticsearchServer.class);
      }

      @Override
      public void onReload(Container container) {

      }

      @Override
      public void onShutdown(Container container) {

      }
    });

    packages("io.jenkins.plugins");
  }

}
