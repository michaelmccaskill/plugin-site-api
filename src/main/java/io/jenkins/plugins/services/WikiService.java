package io.jenkins.plugins.services;

public interface WikiService {

  String getWikiContent(String url) throws ServiceException;

  String cleanWikiContent(String url, String content) throws ServiceException;

}
