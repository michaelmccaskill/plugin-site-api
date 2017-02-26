package io.jenkins.plugins.services;

/**
 * <p>Responsible for populating the datastore</p>
 */
public interface PrepareDatastoreService {

  /**
   * <p>Populate the datastore</p>
   *
   * @throws ServiceException in case something goes wrong
   */
  void populateDataStore() throws ServiceException;

  /**
   * <p>Schedule population of datastore in the future</p>
   *
   * @throws ServiceException in case something goes wrong
   */
  void schedulePopulateDataStore() throws ServiceException;

}
