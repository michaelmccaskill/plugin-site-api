package io.jenkins.plugins.schedule;

import org.glassfish.hk2.utilities.binding.AbstractBinder;

import javax.inject.Singleton;

public class Binder extends AbstractBinder {

  @Override
  protected void configure() {
    bind(JobScheduler.class).to(JobScheduler.class).in(Singleton.class);
  }
}
