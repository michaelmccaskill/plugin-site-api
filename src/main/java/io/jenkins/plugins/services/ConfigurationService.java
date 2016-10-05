package io.jenkins.plugins.services;

/**
 * <p>Get various configuration pieces for the application</p>
 */
public interface ConfigurationService {

  /**
   * <p>Get index data need to populating Elasticsearch</p>
   *
   * @return JSON content
   * @throws ServiceException in case something goes wrong
     */
  String getIndexData() throws ServiceException;

}
