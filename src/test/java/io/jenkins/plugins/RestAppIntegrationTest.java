package io.jenkins.plugins;

import io.jenkins.plugins.models.*;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.jetty.JettyTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.core.Application;

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
    Assert.assertNotNull("Search for 'git' null", plugins);
    Assert.assertTrue("Should return multiple results", plugins.getTotal() > 1);
  }

  @Test
  public void testSearchSortByInstalls() {
    final Plugins plugins = target("/plugins").queryParam("q", "git").queryParam("sort", "installs").request().get(Plugins.class);
    Assert.assertNotNull("Search for 'git' null", plugins);
    Assert.assertTrue("Should return multiple results", plugins.getTotal() > 1);
    Assert.assertTrue("SortBy.INSTALLS not correct", plugins.getPlugins().get(0).getStats().getCurrentInstalls() > plugins.getPlugins().get(1).getStats().getCurrentInstalls());
  }

  @Test
  public void testSearchSortByName() {
    final Plugins plugins = target("/plugins").queryParam("q", "git").queryParam("sort", "name").request().get(Plugins.class);
    Assert.assertNotNull("Search for 'git' null", plugins);
    Assert.assertTrue("Should return multiple results", plugins.getTotal() > 1);
    Assert.assertTrue("SortBy.NAME not correct", plugins.getPlugins().get(0).getName().compareTo(plugins.getPlugins().get(1).getName()) < 0);
  }

  @Test
  public void testSearchSortByRelevance() {
    final Plugins plugins = target("/plugins").queryParam("q", "git").queryParam("sort", "relevance").request().get(Plugins.class);
    Assert.assertNotNull("Search for 'git' null", plugins);
    Assert.assertTrue("Should return multiple results", plugins.getTotal() > 1);
  }

  @Test
  public void testSearchSortByTitle() {
    final Plugins plugins = target("/plugins").queryParam("q", "git").queryParam("sort", "title").request().get(Plugins.class);
    Assert.assertNotNull("Search for 'git' null", plugins);
    Assert.assertTrue("Should return multiple results", plugins.getTotal() > 1);
    Assert.assertTrue("SortBy.TITLE not correct", plugins.getPlugins().get(0).getTitle().compareTo(plugins.getPlugins().get(1).getTitle()) < 0);
  }

  @Test
  public void testSearchSortByTrend() {
    final Plugins plugins = target("/plugins").queryParam("q", "git").queryParam("sort", "trend").request().get(Plugins.class);
    Assert.assertNotNull("Search for 'git' null", plugins);
    Assert.assertTrue("Should return multiple results", plugins.getTotal() > 1);
    Assert.assertTrue("SortBy.TREND not correct", plugins.getPlugins().get(0).getStats().getTrend() > plugins.getPlugins().get(1).getStats().getTrend());
  }

  @Test
  public void testSearchSortByUpdated() {
    final Plugins plugins = target("/plugins").queryParam("q", "git").queryParam("sort", "updated").request().get(Plugins.class);
    Assert.assertNotNull("Search for 'git' null", plugins);
    Assert.assertTrue("Should return multiple results", plugins.getTotal() > 1);
    Assert.assertTrue("SortBy.UPDATED not correct", plugins.getPlugins().get(0).getReleaseTimestamp().isAfter(plugins.getPlugins().get(1).getReleaseTimestamp()));
  }

  @Test
  public void testSearchCategories() {
    final Plugins plugins = target("/plugins").queryParam("categories", "scm").request().get(Plugins.class);
    Assert.assertNotNull("Search for categories 'scm' is null", plugins);
    for (Plugin plugin : plugins.getPlugins()) {
      if (plugin.getLabels().contains("scm")) {
        return;
      }
    }
    Assert.fail("Didn't find plugins with categories 'scm'");
  }

  @Test
  public void testSearchLabels() {
    final Plugins plugins = target("/plugins").queryParam("labels", "scm").request().get(Plugins.class);
    Assert.assertNotNull("Search for labels 'scm' is null", plugins);
    for (Plugin plugin : plugins.getPlugins()) {
      if (plugin.getLabels().contains("scm")) {
        return;
      }
    }
    Assert.fail("Didn't find plugins with labels 'scm'");
  }

  @Test
  public void testSearchAuthors() {
    final Plugins plugins = target("/plugins").queryParam("authors", "Kohsuke Kawaguchi").request().get(Plugins.class);
    Assert.assertNotNull("Search for categories 'scm' is null", plugins);
    for (Plugin plugin : plugins.getPlugins()) {
      for (Developer developer : plugin.getDevelopers()) {
        if (developer.getName().equalsIgnoreCase("Kohsuke Kawaguchi")) {
          return;
        }
      }
    }
    Assert.fail("Didn't find plugins with authors 'Kohsuke Kawaguchi'");
  }

  @Test
  public void testSearchRequiredCore() {
    final Plugins plugins = target("/plugins").queryParam("core", "1.505").request().get(Plugins.class);
    Assert.assertNotNull("Search for requiredCore is null", plugins);
    for (Plugin plugin : plugins.getPlugins()) {
      if (!plugin.getRequiredCore().equals("1.505")) {
        Assert.fail("Found plugin with requiredCore not '1.505'");
      }
    }
  }

  @Test
  public void testGetLabels() throws Exception {
    final Labels labels = target("/labels").request().get(Labels.class);
    Assert.assertNotNull("Labels null", labels);
    Assert.assertFalse("Labels empty", labels.getLabels().isEmpty());
    Assert.assertEquals("Labels limit doesn't match", labels.getLimit(), labels.getLabels().size());
  }

  @Test
  public void testGetCategories() throws Exception {
    final Categories categories = target("/categories").request().get(Categories.class);
    Assert.assertNotNull("Categories null", categories);
    Assert.assertFalse("Categories empty", categories.getCategories().isEmpty());
    Assert.assertEquals("Categories limit doesn't match", categories.getLimit(), categories.getCategories().size());
  }

  @Test
  public void testGetDevelopers() {
    final Developers developers = target("/developers").request().get(Developers.class);
    Assert.assertNotNull("Developers null", developers);
    Assert.assertFalse("Developers empty", developers.getDevelopers().isEmpty());
    Assert.assertEquals("Developers limit doesn't match", developers.getLimit(), developers.getDevelopers().size());
  }

  @Test
  public void testGetMostInstalled() {
    final Plugins plugins = target("/plugins/installed").request().get(Plugins.class);
    Assert.assertNotNull("Most installed null", plugins);
    Assert.assertTrue("Should return multiple results", plugins.getTotal() > 1);
    Assert.assertTrue("Most installed order not correct", plugins.getPlugins().get(0).getStats().getCurrentInstalls() > plugins.getPlugins().get(1).getStats().getCurrentInstalls());
    Assert.assertEquals("Most installed limit doesn't match", plugins.getLimit(), plugins.getPlugins().size());
  }

  @Test
  public void testGetRecentlyUpdated() {
    final Plugins plugins = target("/plugins/updated").request().get(Plugins.class);
    Assert.assertNotNull("Recently updated null", plugins);
    Assert.assertTrue("Should return multiple results", plugins.getTotal() > 1);
    Assert.assertTrue("Recently updated order not correct", plugins.getPlugins().get(0).getReleaseTimestamp().isAfter(plugins.getPlugins().get(1).getReleaseTimestamp()));
    Assert.assertEquals("Recently updated limit doesn't match", plugins.getLimit(), plugins.getPlugins().size());
  }

  @Test
  public void testGetTrend() {
    final Plugins plugins = target("/plugins/trend").request().get(Plugins.class);
    Assert.assertNotNull("Trend null", plugins);
    Assert.assertTrue("Should return multiple results", plugins.getTotal() > 1);
    Assert.assertTrue("Trend order not correct", plugins.getPlugins().get(0).getStats().getTrend() > plugins.getPlugins().get(1).getStats().getTrend());
    Assert.assertEquals("Trend limit doesn't match", plugins.getLimit(), plugins.getPlugins().size());

  }

  @Test
  public void testGetVersions() {
    final Versions versions = target("/versions").request().get(Versions.class);
    Assert.assertNotNull("Versions null", versions);
    Assert.assertFalse("Versions empty", versions.getVersions().isEmpty());
    Assert.assertEquals("Versions limit doesn't match", versions.getLimit(), versions.getVersions().size());
  }

}
