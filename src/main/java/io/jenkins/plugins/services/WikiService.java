package io.jenkins.plugins.services;

import io.jenkins.plugins.models.Plugin;

public interface WikiService {

  String getWikiContent(Plugin plugin) throws ServiceException;

  String cleanWikiContent(String content) throws ServiceException;

}
