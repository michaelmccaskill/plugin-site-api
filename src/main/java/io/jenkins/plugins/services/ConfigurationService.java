package io.jenkins.plugins.services;

import io.jenkins.plugins.models.GeneratedPluginData;

/**
 * <p>Get various configuration pieces for the application</p>
 */
public interface ConfigurationService {

  /**
   * <p>Get index data need to populating Elasticsearch</p>
   *
   * @return GeneratedPluginData, null if it hasn't changed since last time called
   * @throws ServiceException in case something goes wrong
     */
  GeneratedPluginData getIndexData() throws ServiceException;

}
