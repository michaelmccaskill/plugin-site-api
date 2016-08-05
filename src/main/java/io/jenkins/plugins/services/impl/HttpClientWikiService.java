package io.jenkins.plugins.services.impl;

import io.jenkins.plugins.models.Plugin;
import io.jenkins.plugins.services.ServiceException;
import io.jenkins.plugins.services.WikiService;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class HttpClientWikiService implements WikiService {

  private Logger logger = LoggerFactory.getLogger(HttpClientWikiService.class);

  @Override
  public String getWikiContent(Plugin plugin) throws ServiceException {
    if (plugin.getWiki().getUrl() != null && !plugin.getWiki().getUrl().isEmpty()) {
      final CloseableHttpClient httpClient = HttpClients.createDefault();
      try {
        return doGetWikiContent(httpClient, plugin.getWiki().getUrl(), true);
      } catch (Exception e) {
        logger.error("Problem getting wiki content", e);
        throw new ServiceException("Problem getting wiki content", e);
      } finally {
        try {
          httpClient.close();
        } catch (IOException e) {
          logger.warn("Problem closing HttpClient", e);
        }
      }
    } else {
      return null;
    }
  }

  private String doGetWikiContent(CloseableHttpClient httpClient, String url, boolean follow) throws Exception {
    final HttpGet get = new HttpGet(url);
    final CloseableHttpResponse response = httpClient.execute(get);
    try {
      switch (response.getStatusLine().getStatusCode()) {
        case HttpStatus.SC_MOVED_PERMANENTLY:
        case HttpStatus.SC_MOVED_TEMPORARILY:
        case HttpStatus.SC_SEE_OTHER:
          if (follow) {
            return doGetWikiContent(httpClient, response.getFirstHeader("Location").getValue(), false);
          } else {
            logger.warn("Already tried to follow to get wiki content.");
            return null;
          }
        case HttpStatus.SC_OK:
          final HttpEntity entity = response.getEntity();
          final String html = EntityUtils.toString(entity);
          EntityUtils.consume(entity);
          return html;
        default:
          logger.warn("Unable to get content from " + url);
          return null;
      }
    } finally {
      try {
        response.close();
      } catch (IOException e) {
        logger.warn("Problem closing response", e);
      }
    }
  }

  @Override
  public String cleanWikiContent(String content) throws ServiceException {
    if (content == null || content.trim().isEmpty()) {
      logger.warn("Can't clean null content");
      return null;
    }
    final Document html = Jsoup.parse(content);
    final Elements elements = html.getElementsByClass("wiki-content");
    if (elements.isEmpty()) {
      logger.warn("wiki-content not found in content");
      return null;
    }
    elements.first().getElementsByAttribute("href").forEach((element) -> replaceAttribute(element, "href"));
    elements.first().getElementsByAttribute("src").forEach((element) -> replaceAttribute(element, "src"));
    return elements.first().html();
  }

  private void replaceAttribute(Element element, String attributeName) {
    final String attribute = element.attr(attributeName);
    if (attribute.startsWith("/")) {
      element.attr(attributeName, "https://wiki.jenkins-ci.org" + attribute);
    }
  }

}
