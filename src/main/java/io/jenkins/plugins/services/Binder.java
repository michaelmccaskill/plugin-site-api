package io.jenkins.plugins.services;

import io.jenkins.plugins.services.impl.ElasticsearchDatastoreService;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import javax.inject.Singleton;

public class Binder extends AbstractBinder {

  @Override
  protected void configure() {
    bind(ElasticsearchDatastoreService.class).to(DatastoreService.class).in(Singleton.class);
  }
}
