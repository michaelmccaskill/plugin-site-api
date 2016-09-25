package io.jenkins.plugins.services;

/**
 * <p>Responsible for retrieving and cleaning wiki content for a plugin</p>
 */
public interface WikiService {

  /**
   * <p>Get wiki content</p>
   *
   * @param url URL to the wiki content`
   * @return content
   * @throws ServiceException in case something goes wrong
   */
  String getWikiContent(String url) throws ServiceException;

  /**
   * <p>Clean wiki content so it's presentable to the UI</p>
   *
   * @param url URL to the wiki content
   * @param content Wiki content
   * @return cleaned content
   * @throws ServiceException in case something goes wrong
   */
  String cleanWikiContent(String url, String content) throws ServiceException;

}
