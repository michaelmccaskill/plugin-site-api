package io.jenkins.plugins.services.impl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
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

import javax.annotation.PostConstruct;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * <p>Implementation of <code>WikiService</code> powered by <code>HttpClient</code></p>
 *
 * <p>For performance reasons the content for a plugin url is cached using a <code>LoadingCache</code> for 6 hours</p>
 */
public class HttpClientWikiService implements WikiService {

  private Logger logger = LoggerFactory.getLogger(HttpClientWikiService.class);

  private LoadingCache<String, String> wikiContentCache;

  public static final String WIKI_URL = "https://wiki.jenkins-ci.org";

  @PostConstruct
  public void postConstruct() {
    wikiContentCache = CacheBuilder.newBuilder()
      .expireAfterWrite(6, TimeUnit.HOURS)
      .maximumSize(1000)
      .build(new CacheLoader<String, String>() {
        @Override
        public String load(String url) throws Exception {
          // Load the wiki content then clean it
          final String rawContent = doGetWikiContent(url);
          return cleanWikiContent(rawContent);
        }
      });
  }

  @Override
  public String getWikiContent(String url) throws ServiceException {
    if (url != null && !url.trim().isEmpty()) {
      try {
        // This is what fires the CacheLoader that's defined in the postConstruct.
        return wikiContentCache.get(url);
      } catch (Exception e) {
        logger.error("Problem getting wiki content", e);
        return null;
      }
    } else {
      return null;
    }
  }

  private String doGetWikiContent(String url) {
    final HttpGet get = new HttpGet(url);
    try (final CloseableHttpClient httpClient = HttpClients.createDefault(); final CloseableHttpResponse response = httpClient.execute(get)) {
      if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
        final HttpEntity entity = response.getEntity();
        final String html = EntityUtils.toString(entity);
        EntityUtils.consume(entity);
        return html;
      } else {
        logger.warn("Unable to get content from {} - returned status code {}", url, response.getStatusLine().getStatusCode());
        return null;
      }
    } catch (Exception e) {
      logger.error("Problem getting wiki content", e);
      return null;
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
    final Element wikiContent = elements.first();
    // This removes specific Confluence elements not needed by the front end
    wikiContent.getElementsByClass("table-wrap").remove();
    // Replace href/src with the wiki url
    wikiContent.getElementsByAttribute("href").forEach(element -> replaceAttribute(element, "href"));
    wikiContent.getElementsByAttribute("src").forEach(element -> replaceAttribute(element, "src"));
    return wikiContent.html();
  }

  public void replaceAttribute(Element element, String attributeName) {
    final String attribute = element.attr(attributeName);
    if (attribute.startsWith("/")) {
      element.attr(attributeName, WIKI_URL + attribute);
    }
  }

}
