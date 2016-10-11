package io.jenkins.plugins.services;

import io.jenkins.plugins.commons.JsonObjectMapper;
import io.jenkins.plugins.models.GeneratedPluginData;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * <p>Mocked ConfigurationService</p>
 */
public class MockConfigurationService implements ConfigurationService {

  private final Logger logger = LoggerFactory.getLogger(MockConfigurationService.class);

  @Override
  public GeneratedPluginData getIndexData() throws ServiceException {
    try {
      logger.info("Using test plugin data");
      final ClassLoader cl = getClass().getClassLoader();
      final File dataFile = new File(cl.getResource("plugins.json").getFile());
      final String data = FileUtils.readFileToString(dataFile, "utf-8");
      return JsonObjectMapper.getObjectMapper().readValue(data, GeneratedPluginData.class);
    } catch (Exception e) {
      throw new RuntimeException("Can't get test plugin data");
    }
  }
}
