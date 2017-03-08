package io.jenkins.plugins.services;

import io.jenkins.plugins.models.*;

/**
 * <p>Contract for retrieving various domain models in the application</p>
 */
public interface DatastoreService {

  /**
   * <p>Search for plugins based on <code>SearchOptions</code>
   *
   * @param searchOptions Various criteria for matching plugins
   * @return matching plugins
   * @throws ServiceException in case something goes wrong
   * @see SearchOptions
   */
  Plugins search(SearchOptions searchOptions) throws ServiceException;

  /**
   * <p>Get a plugin by name</p>
   *
   * @param name Plugin name
   * @return matching plugin
   * @throws ServiceException in case something goes wrong
   */
  Plugin getPlugin(String name) throws ServiceException;

  /**
   * <p>Get unique categories for the application</p>
   *
   * @throws ServiceException in case something goes wrong
   * @return categories
   */
  Categories getCategories() throws ServiceException;

  /**
   * <p>Get unique labels for the application</p>
   *
   * @throws ServiceException in case something goes wrong
   * @return labels
   */
  Labels getLabels() throws ServiceException;

  /**
   * <p>Get unique maintainers for the application</p>
   *
   * @throws ServiceException in case something goes wrong
   * @return maintainers
   */
  Maintainers getMaintainers() throws ServiceException;


  /**
   * <p>Get unique versions for the application</p>
   *
   * @throws ServiceException in case something goes wrong
   * @return versions
   */
  Versions getVersions() throws ServiceException;

}
