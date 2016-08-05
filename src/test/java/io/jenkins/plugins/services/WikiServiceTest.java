package io.jenkins.plugins.services;

import io.jenkins.plugins.services.impl.HttpClientWikiService;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class WikiServiceTest {

  private WikiService wikiService;

  @Before
  public void setUp() {
    wikiService = new HttpClientWikiService();
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
    final File file = new File("src/test/resources/wiki_content.html");
    final String content = FileUtils.readFileToString(file, "utf-8");
    final String cleanContent = wikiService.cleanWikiContent(content);
    Assert.assertNotNull("Wiki content is null", cleanContent);
    Assert.assertFalse("Wiki content not clean - href references to root", cleanContent.matches("href=\"/"));
    Assert.assertFalse("Wiki content not clean - src references to root", cleanContent.matches("src=\"/"));
    Assert.assertFalse("Wiki content not clean - references to table-wrap", cleanContent.matches("class=\"table-wrap\""));
  }

}
