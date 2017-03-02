package io.jenkins.plugins.services;

import io.jenkins.plugins.services.impl.HttpClientWikiService;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class WikiServiceTest {

  private HttpClientWikiService wikiService;

  @Before
  public void setUp() {
    wikiService = new HttpClientWikiService();
    wikiService.postConstruct();
  }

  @Test
  public void testGetWikiContent() {
    final String url = "https://wiki.jenkins-ci.org/display/JENKINS/Git+Plugin";
    final String content = wikiService.getWikiContent(url);
    Assert.assertNotNull("Wiki content is null", content);
    Assert.assertFalse("Wiki content is empty", content.isEmpty());
  }

  @Test
  public void testGetWikiContentNon200() {
    final String url = "http://httpstat.us/500";
    final String content = wikiService.getWikiContent(url);
    Assert.assertNull(content);
  }

  @Test
  public void testCleanWikiContent() throws IOException {
    final File file = new File("src/test/resources/wiki_content.html");
    final String content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
    final String cleanContent = wikiService.cleanWikiContent(content);
    Assert.assertNotNull("Wiki content is null", cleanContent);
    final Document html = Jsoup.parseBodyFragment(cleanContent);
    html.getElementsByAttribute("href").forEach(element -> {
      final String value = element.attr("href");
      Assert.assertFalse("Wiki content not clean - href references to root : " + value, value.startsWith("/"));
    });
    html.getElementsByAttribute("src").forEach(element -> {
      final String value = element.attr("src");
      Assert.assertFalse("Wiki content not clean - src references to root : " + value, value.startsWith("/"));
    });
    Assert.assertTrue("Wiki content not clean - references to table-wrap", html.getElementsByClass("table-wrap").isEmpty());
  }

  @Test
  public void testReplaceAttribute() throws IOException {
    final String src = "/some-image.jpg";
    final Element element = Jsoup.parseBodyFragment(String.format("<img id=\"test-image\" src=\"%s\"/>", src)).getElementById("test-image");
    wikiService.replaceAttribute(element, "src");
    Assert.assertEquals("Attribute replacement failed", HttpClientWikiService.WIKI_URL + src, element.attr("src"));
  }

}
