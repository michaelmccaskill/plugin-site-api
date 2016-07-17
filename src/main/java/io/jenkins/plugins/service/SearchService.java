package io.jenkins.plugins.service;

import org.json.JSONObject;

import java.util.List;

public interface SearchService {

  JSONObject search(String query, String sort, List<String> labels, List<String> authors, String core, Integer size, Integer page) throws ServiceException;

}
