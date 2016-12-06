package io.jenkins.plugins.services.impl;

import io.jenkins.plugins.commons.JsonObjectMapper;
import io.jenkins.plugins.models.GeneratedPluginData;
import io.jenkins.plugins.services.ConfigurationService;
import io.jenkins.plugins.services.ServiceException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

/**
 * <p>Default implementation of <code>ConfigurationService</code></p>
 */
public class DefaultConfigurationService implements ConfigurationService {

  private final Logger logger = LoggerFactory.getLogger(DefaultConfigurationService.class);

  @Override
  public GeneratedPluginData getIndexData() throws ServiceException {
    final CloseableHttpClient httpClient = HttpClients.createDefault();
    try {
      final String url = getDataFileUrl();
      final HttpGet get = new HttpGet(url);
      final CloseableHttpResponse response = httpClient.execute(get);
      if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
        final HttpEntity entity = response.getEntity();
        final InputStream inputStream = entity.getContent();
        final File dataFile = File.createTempFile("plugins", ".json.gzip");
        FileUtils.copyToFile(inputStream, dataFile);
        final String data = readGzipFile(dataFile);
        return JsonObjectMapper.getObjectMapper().readValue(data, GeneratedPluginData.class);
      } else {
        logger.error("Data file not found");
        throw new RuntimeException("Data file not found");
      }
    } catch (Exception e) {
      logger.error("Problem getting data file", e);
      throw new ServiceException("Problem getting data file", e);
    } finally {
      try {
        httpClient.close();
      } catch (IOException e) {
        logger.warn("Problem closing HttpClient", e);
      }
    }
  }

  private String getDataFileUrl() {
    if (System.getenv().containsKey("DATA_FILE_URL")) {
      final String url = StringUtils.trimToNull(System.getenv("DATA_FILE_URL"));
      if (url == null) {
        throw new RuntimeException("Environment variable 'DATA_FILE_URL' is empty");
      }
      return url;
    } else {
      final String url = StringUtils.trimToNull(System.getProperty("data.file.url"));
      if (url == null) {
        throw new RuntimeException("System property 'data.file.url' is not given");
      }
      return url;
    }
  }

  private String readGzipFile(final File file) {
    try(final BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file)), StandardCharsets.UTF_8))) {
      return reader.lines().collect(Collectors.joining());
    } catch (Exception e) {
      logger.error("Problem decompressing plugin data", e);
      throw new RuntimeException("Problem decompressing plugin data", e);
    }
  }

}
