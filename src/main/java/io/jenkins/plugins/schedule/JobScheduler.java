package io.jenkins.plugins.schedule;

import org.elasticsearch.client.Client;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

public class JobScheduler {

  private static final JobKey JOB_KEY = JobKey.jobKey("populateElasticsearchJob");

  private final Logger logger = LoggerFactory.getLogger(JobScheduler.class);

  private Scheduler scheduler;

  @Inject
  private Client esClient;

  @PostConstruct
  public void postConstruct() {
    try {
      this.scheduler = StdSchedulerFactory.getDefaultScheduler();
      this.scheduler.start();
    } catch (SchedulerException e) {
      logger.error("Problem initializing Quartz", e);
    }
    schedule();
  }

  @PreDestroy
  public void preDestroy() {
    try {
      if (!this.scheduler.isShutdown()) {
        this.scheduler.shutdown();
      }
    } catch (SchedulerException e) {
        logger.error("Problem shutting down Quartz", e);
    }
  }

  private void schedule() {
    final JobDataMap dataMap = new JobDataMap();
    dataMap.put(PopulateElasticsearchJob.ES_CLIENT_KEY, esClient);
    final JobDetail job = JobBuilder
      .newJob(PopulateElasticsearchJob.class)
      .withIdentity(JOB_KEY)
      .usingJobData(dataMap)
      .build();
    final Trigger trigger = TriggerBuilder
      .newTrigger()
      .withIdentity("populateElasticserchTrigger")
      // Fire now and then every 12 hours
      .withSchedule(SimpleScheduleBuilder.simpleSchedule()
        .withIntervalInHours(12)
        .repeatForever()
      )
      .build();
    try {
      if (!scheduler.checkExists(JOB_KEY)) {
        scheduler.scheduleJob(job, trigger);
      }  else {
        logger.info("Already scheduled");
      }
    } catch (SchedulerException e) {
      logger.error("Problem scheduling " + JOB_KEY + " + job");
    }
  }

}
