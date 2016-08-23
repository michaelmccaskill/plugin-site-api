package io.jenkins.plugins.services;

import io.jenkins.plugins.services.impl.HttpClientWikiService;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

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
  public void testCleanWikiContent() throws IOException {
    final String url = "https://wiki.jenkins-ci.org/display/JENKINS/Git+Plugin";
    final File file = new File("src/test/resources/wiki_content.html");
    final String content = FileUtils.readFileToString(file, "utf-8");
    final String cleanContent = wikiService.cleanWikiContent(url, content);
    Assert.assertNotNull("Wiki content is null", cleanContent);
    final Document html = Jsoup.parse(content);
    html.getElementsByAttribute("href").forEach((element) -> {
      final String value = element.attr("href");
      Assert.assertFalse("Wiki content not clean - href references to root or hash", value.startsWith("/") && value.startsWith("#"));
    });
    html.getElementsByAttribute("href").forEach((element) -> {
      final String value = element.attr("src");
      Assert.assertFalse("Wiki content not clean - src references to root", value.startsWith("/"));
    });
    Assert.assertTrue("Wiki content not clean - references to table-wrap", html.getElementsByAttributeValue("class", "tabke-wrap").size() == 0);
  }

}
