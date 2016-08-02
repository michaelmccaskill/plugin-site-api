package io.jenkins.plugins.datastore;

import io.jenkins.plugins.models.*;

import java.util.List;

public interface DatastoreService {

  Plugins search(String query, SortBy sortBy, List<String> categories, List<String> labels, List<String> authors, String core, Integer size, Integer page) throws DatastoreException;

  Plugin getPlugin(String name) throws DatastoreException;

  Categories getCategories() throws DatastoreException;

  Developers getDevelopers() throws DatastoreException;

  Labels getLabels() throws DatastoreException;

}
