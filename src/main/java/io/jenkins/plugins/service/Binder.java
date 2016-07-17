package io.jenkins.plugins.service;

import io.jenkins.plugins.service.impl.ElasticsearchSearchService;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import javax.inject.Singleton;

public class Binder extends AbstractBinder {

  @Override
  protected void configure() {
    bind(ElasticsearchSearchService.class).to(SearchService.class).in(Singleton.class);
  }
}
