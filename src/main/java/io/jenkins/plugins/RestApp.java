package io.jenkins.plugins;

import io.jenkins.plugins.datastore.Binder;
import io.jenkins.plugins.schedule.JobScheduler;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spi.Container;
import org.glassfish.jersey.server.spi.ContainerLifecycleListener;

import javax.ws.rs.ApplicationPath;

@ApplicationPath("/")
public class RestApp extends ResourceConfig {

  public RestApp() {

    register(new Binder());
    register(new io.jenkins.plugins.schedule.Binder());

    register(new ContainerLifecycleListener() {
      @Override
      public void onStartup(Container container) {
        final ServiceLocator locator = container.getApplicationHandler().getServiceLocator();
        // Preload JobScheduler so job is scheduled
        locator.getService(JobScheduler.class);
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
