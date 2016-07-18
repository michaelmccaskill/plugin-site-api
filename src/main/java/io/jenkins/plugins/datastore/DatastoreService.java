package io.jenkins.plugins.datastore;

import org.json.JSONObject;

import java.util.List;

public interface DatastoreService {

  JSONObject search(String query, String sort, List<String> labels, List<String> authors, String core, Integer size, Integer page) throws DatastoreException;

  JSONObject getPlugin(String name) throws DatastoreException;

  JSONObject getCategories() throws DatastoreException;

  JSONObject getLabels() throws DatastoreException;

}
