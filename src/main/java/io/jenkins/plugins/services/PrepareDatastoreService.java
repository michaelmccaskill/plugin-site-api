package io.jenkins.plugins.services;

import java.time.LocalDateTime;

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

  /**
   * <p>Time the current index was created</p>
   */
  LocalDateTime getCurrentCreatedAt();

}
