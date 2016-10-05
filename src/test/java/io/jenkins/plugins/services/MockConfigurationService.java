package io.jenkins.plugins.services;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class MockConfigurationService implements ConfigurationService {

  private final Logger logger = LoggerFactory.getLogger(MockConfigurationService.class);

  @Override
  public String getIndexData() throws ServiceException {
    try {
      logger.info("Using test plugin data");
      final ClassLoader cl = getClass().getClassLoader();
      final File mappingFile = new File(cl.getResource("plugins.json").getFile());
      return FileUtils.readFileToString(mappingFile, "utf-8");
    } catch (Exception e) {
      throw new RuntimeException("Can't get test plugin data");
    }
  }
}
