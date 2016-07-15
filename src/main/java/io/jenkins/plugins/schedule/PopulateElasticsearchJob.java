package io.jenkins.plugins.schedule;

import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.Client;
import org.json.JSONArray;
import org.json.JSONObject;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class PopulateElasticsearchJob implements Job {

  public static String ES_CLIENT_KEY = "esClient";

  private final Logger logger = LoggerFactory.getLogger(PopulateElasticsearchJob.class);

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    final Client esClient = (Client)context.getJobDetail().getJobDataMap().get(ES_CLIENT_KEY);
    if (Objects.isNull(esClient)) {
      throw new JobExecutionException(ES_CLIENT_KEY + " is null");
    }
    logger.info("Starting PopulateElasticsearchJob");
    final int processors = Runtime.getRuntime().availableProcessors();
    final ExecutorService executorService = Executors.newFixedThreadPool(processors * 10);
    final CloseableHttpClient httpClient = HttpClients.createDefault();
    try {
      final ResponseHandler<JSONObject> updateCenterHandler = (httpResponse) -> {
        final StatusLine status = httpResponse.getStatusLine();
        if (status.getStatusCode() == 200) {
          final HttpEntity entity = httpResponse.getEntity();
          final String content = EntityUtils.toString(entity);
          try {
            return new JSONObject(String.join("", content.split("\n")[1])).getJSONObject("plugins");
          } catch (Exception e) {
            throw new ClientProtocolException("Update center returned invalid JSON");
          }
        } else {
          throw new ClientProtocolException("Unexpected response from update center - " + status.toString());
        }
      };
      final JSONObject plugins = httpClient.execute(new HttpGet("https://updates.jenkins-ci.org/current/update-center.json"), updateCenterHandler);
      final List<Callable<Void>> tasks = new ArrayList<>();
      for (String key : plugins.keySet()) {
        final JSONObject plugin = plugins.getJSONObject(key);
        final Callable<Void> task = () -> {
          final ResponseHandler<Void> statsHandler = (httpResponse) -> {
            final StatusLine status = httpResponse.getStatusLine();
            if (status.getStatusCode() == 200) {
              final HttpEntity entity = httpResponse.getEntity();
              final String content = EntityUtils.toString(entity);
              try {
                final JSONObject stats = new JSONObject(content);
                final JSONObject installations = stats.getJSONObject("installations");
                final JSONObject installationsPercentage = stats.getJSONObject("installationsPercentage");
                final JSONObject installationsPerVersion = stats.getJSONObject("installationsPerVersion");
                final JSONObject installationsPercentagePerVersion = stats.getJSONObject("installationsPercentagePerVersion");
                plugin.put("installations", new JSONArray(installations.keySet().stream().map((timestamp) -> {
                  final JSONObject installation = new JSONObject();
                  installation.put("timestamp", Long.valueOf(timestamp));
                  installation.put("total", installations.getInt(timestamp));
                  return installation;
                }).collect(Collectors.toSet())));
                plugin.put("installationsPercentage", new JSONArray(installationsPercentage.keySet().stream().map((timestamp) -> {
                  final JSONObject installation = new JSONObject();
                  installation.put("timestamp", Long.valueOf(timestamp));
                  installation.put("percentage", installationsPercentage.getDouble(timestamp));
                  return installation;
                }).collect(Collectors.toSet())));
                plugin.put("installationsPerVersion", new JSONArray(installationsPerVersion.keySet().stream().map((version) -> {
                  final JSONObject installation = new JSONObject();
                  installation.put("version", version);
                  installation.put("total", installationsPerVersion.getInt(version));
                  return installation;
                }).collect(Collectors.toSet())));
                plugin.put("installationsPercentagePerVersion", new JSONArray(installationsPercentagePerVersion.keySet().stream().map((version) -> {
                  final JSONObject installation = new JSONObject();
                  installation.put("version", version);
                  installation.put("total", installationsPercentagePerVersion.getDouble(version));
                  return installation;
                }).collect(Collectors.toSet())));
                return null;
              } catch (Exception e) {
                throw new ClientProtocolException("Problem processing stats", e);
              }
            } else {
              throw new ClientProtocolException("Unexpected response for stats " + status.toString());
            }
          };
          final String url = "http://stats.jenkins-ci.org/plugin-installation-trend/" + plugin.get("name") + ".stats.json";
          try {
            httpClient.execute(new HttpGet(url), statsHandler);
          } catch (Exception e) {
            logger.error("Problem getting status for plugin " + key);
          }
          return null;
        };
        tasks.add(task);
      }
      executorService.invokeAll(tasks).stream().map(task -> {
        try {
          return task.get();
        } catch (Exception e) {
          throw new IllegalStateException(e);
        }
      });
      final BulkProcessor bulk = BulkProcessor.builder(esClient, new BulkProcessor.Listener() {

        @Override
        public void beforeBulk(long executionId, BulkRequest request) {
        }

        @Override
        public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
          logger.info("Indexed " + response.getItems().length + " plugins in " + response.getTook().minutes() + " min " + response.getTook().seconds() + " sec");
        }

        @Override
        public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
        }
      }).build();
      plugins.keySet().forEach((key) -> {
        final JSONObject plugin = plugins.getJSONObject(key);
        bulk.add(esClient.prepareIndex("plugins", "plugins").setId(plugin.getString("name")).setSource(plugin.toString()).request());
      });
      bulk.awaitClose(2, TimeUnit.MINUTES);
      logger.info("Finished PopulateElasticsearchJob");
    } catch (Exception e) {
      logger.error("Problem getting plugin information", e);
    } finally {
      try {
        httpClient.close();
      } catch (IOException e) {
        logger.error("Problem closing httpClient", e);
      }
      if (!executorService.isShutdown()) {
        executorService.shutdown();
      }
    }
  }
}
