package io.jenkins.plugins;

import io.jenkins.plugins.datastore.SortBy;
import io.jenkins.plugins.models.Categories;
import io.jenkins.plugins.models.Labels;
import io.jenkins.plugins.models.Plugin;
import io.jenkins.plugins.models.Plugins;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.jetty.JettyTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.core.Application;
import java.util.Collections;

public class RestAppIntegrationTest extends JerseyTest {

  @Override
  protected Application configure() {
    return new RestApp();
  }

  @Override
  protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
    return new JettyTestContainerFactory();
  }

  @Test
  public void testGetPlugin() {
    final Plugin plugin = target("/plugin/git").request().get(Plugin.class);
    Assert.assertNotNull("Git plugin not found", plugin);
    Assert.assertEquals("git", plugin.getName());
    Assert.assertFalse("Categories are empty", plugin.getCategories().isEmpty());
    Assert.assertFalse("Dependencies are empty", plugin.getDependencies().isEmpty());
    Assert.assertFalse("Developers are empty", plugin.getDevelopers().isEmpty());
    Assert.assertFalse("Labels are empty", plugin.getLabels().isEmpty());
    Assert.assertNotNull("Stats are null", plugin.getStats());
  }

  @Test
  public void testGetPlugins() {
    final Plugins plugins = target("/plugins").queryParam("q", "git").request().get(Plugins.class);
    Assert.assertNotNull("Search for 'git' null'", plugins);
    Assert.assertTrue("Should return multiple results", plugins.getTotal() > 1);
  }

  @Test
  public void testSearchSortByInstalls() {
    final Plugins plugins = target("/plugins").queryParam("q", "git").queryParam("sort", "installs").request().get(Plugins.class);
    Assert.assertNotNull("Search for 'git' null'", plugins);
    Assert.assertTrue("Should return multiple results", plugins.getTotal() > 1);
    Assert.assertTrue("SortBy.INSTALLS not correct", plugins.getPlugins().get(0).getStats().getLifetime() > plugins.getPlugins().get(1).getStats().getLifetime());
  }

  @Test
  public void testSearchSortByName() {
    final Plugins plugins = target("/plugins").queryParam("q", "git").queryParam("sort", "name").request().get(Plugins.class);
    Assert.assertNotNull("Search for 'git' null'", plugins);
    Assert.assertTrue("Should return multiple results", plugins.getTotal() > 1);
    Assert.assertTrue("SortBy.NAME not correct", plugins.getPlugins().get(0).getName().compareTo(plugins.getPlugins().get(1).getName()) < 0);
  }

  @Test
  public void testSearchSortByRelevance() {
    final Plugins plugins = target("/plugins").queryParam("q", "git").queryParam("sort", "relevance").request().get(Plugins.class);
    Assert.assertNotNull("Search for 'git' null'", plugins);
    Assert.assertTrue("Should return multiple results", plugins.getTotal() > 1);
  }

  @Test
  public void testSearchSortByUpdated() {
    final Plugins plugins = target("/plugins").queryParam("q", "git").queryParam("sort", "updated").request().get(Plugins.class);
    Assert.assertNotNull("Search for 'git' null'", plugins);
    Assert.assertTrue("Should return multiple results", plugins.getTotal() > 1);
    Assert.assertTrue("SortBy.UPDATED not correct", plugins.getPlugins().get(0).getReleaseTimestamp().isAfter(plugins.getPlugins().get(1).getReleaseTimestamp()));
  }

  @Test
  public void testGetLabels() throws Exception {
    final Labels labels = target("/labels").request().get(Labels.class);
    Assert.assertNotNull("Labels null'", labels);
    Assert.assertFalse("Labels empty", labels.getLabels().isEmpty());
  }

  @Test
  public void testGetCategories() throws Exception {
    final Categories categories = target("/categories").request().get(Categories.class);
    Assert.assertNotNull("Categories null'", categories);
    Assert.assertFalse("Categories empty", categories.getCategories().isEmpty());
  }

}
