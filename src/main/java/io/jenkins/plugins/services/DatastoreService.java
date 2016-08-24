package io.jenkins.plugins.services;

import io.jenkins.plugins.models.*;

public interface DatastoreService {

  Plugins search(SearchOptions searchOptions) throws ServiceException;

  Plugin getPlugin(String name) throws ServiceException;

  Categories getCategories() throws ServiceException;

  Maintainers getMaintainers() throws ServiceException;

  Labels getLabels() throws ServiceException;

  Versions getVersions() throws ServiceException;

}
