package io.jenkins.plugins.datastore;

import io.jenkins.plugins.models.*;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

public class DatastoreServiceTest {

  private static ServiceLocator locator;
  private static DatastoreService datastoreService;

  // BeforeClass because there's no reason to start/stop Elasticsearch for every test when
  // all our operations are read-only
  @BeforeClass
  public static void setUp() throws Exception {
    locator  = ServiceLocatorUtilities.bind(new Binder());
    datastoreService = locator.getService(DatastoreService.class);
  }

  @AfterClass
  public static void tearDown() throws Exception {
    locator.shutdown();
  }

  @Test
  public void testGetPlugin() {
    final Plugin plugin = datastoreService.getPlugin("git");
    Assert.assertNotNull("Git plugin not found", plugin);
    Assert.assertEquals("git", plugin.getName());
    Assert.assertFalse("Categories are empty", plugin.getCategories().isEmpty());
    Assert.assertFalse("Dependencies are empty", plugin.getDependencies().isEmpty());
    Assert.assertFalse("Developers are empty", plugin.getDevelopers().isEmpty());
    Assert.assertFalse("Labels are empty", plugin.getLabels().isEmpty());
    Assert.assertNotNull("Stats are null", plugin.getStats());
  }

  @Test
  public void testSearch() {
    final Plugins plugins = datastoreService.search(new SearchOptions("git", null, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), null, 50, 1));
    Assert.assertNotNull("Search for 'git' is null", plugins);
    Assert.assertTrue("Should return multiple results", plugins.getTotal() > 1);
  }

  @Test
  public void testSearchSortByInstalls() {
    final Plugins plugins = datastoreService.search(new SearchOptions("git", SortBy.INSTALLS, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), null, 50, 1));
    Assert.assertNotNull("Search for 'git' sort by installs is null", plugins);
    Assert.assertTrue("Should return multiple results", plugins.getTotal() > 1);
    Assert.assertTrue("SortBy.INSTALLS not correct", plugins.getPlugins().get(0).getStats().getLifetime() > plugins.getPlugins().get(1).getStats().getLifetime());
  }

  @Test
  public void testSearchSortByName() {
    final Plugins plugins = datastoreService.search(new SearchOptions("git", SortBy.NAME,Collections.emptyList(),  Collections.emptyList(), Collections.emptyList(), null, 50, 1));
    Assert.assertNotNull("Search for 'git' sort by name is null", plugins);
    Assert.assertTrue("Should return multiple results", plugins.getTotal() > 1);
    Assert.assertTrue("SortBy.NAME not correct", plugins.getPlugins().get(0).getName().compareTo(plugins.getPlugins().get(1).getName()) < 0);
  }

  @Test
  public void testSearchSortByRelevance() {
    final Plugins plugins = datastoreService.search(new SearchOptions("git", SortBy.RELEVANCE, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), null, 50, 1));
    Assert.assertNotNull("Search for 'git' sort by relevance is null", plugins);
    Assert.assertTrue("Should return multiple results", plugins.getTotal() > 1);
  }

  @Test
  public void testSearchSortByUpdated() {
    final Plugins plugins = datastoreService.search(new SearchOptions("git", SortBy.UPDATED, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), null, 50, 1));
    Assert.assertNotNull("Search for 'git' sort by updated is null", plugins);
    Assert.assertTrue("Should return multiple results", plugins.getTotal() > 1);
    Assert.assertTrue("SortBy.UPDATED not correct", plugins.getPlugins().get(0).getReleaseTimestamp().isAfter(plugins.getPlugins().get(1).getReleaseTimestamp()));
  }

  @Test
  public void testSearchCategories() {
    final Plugins plugins = datastoreService.search(new SearchOptions(null, null, Arrays.asList("scm"), Collections.emptyList(), Collections.emptyList(), null, 50, 1));
    Assert.assertNotNull("Search for categories 'scm' is null", plugins);
    for (Plugin plugin : plugins.getPlugins()) {
      if (plugin.getCategories().contains("scm")) {
        return;
      }
    }
    Assert.fail("Didn't find plugins with categories 'scm'");
  }

  @Test
  public void testSearchLabels() {
    final Plugins plugins = datastoreService.search(new SearchOptions(null, null, Collections.emptyList(), Arrays.asList("scm"), Collections.emptyList(), null, 50, 1));
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
    final Plugins plugins = datastoreService.search(new SearchOptions(null, null, Collections.emptyList(), Collections.emptyList(), Arrays.asList("Kohsuke Kawaguchi"), null, 50, 1));
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
    final Plugins plugins = datastoreService.search(new SearchOptions(null , null, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), "1.505", 50, 1));
    Assert.assertNotNull("Search for requiredCore is null", plugins);
    for (Plugin plugin : plugins.getPlugins()) {
      if (!plugin.getRequiredCore().equals("1.505")) {
        Assert.fail("Found plugin with requiredCore not '1.505'");
      }
    }
  }

  @Test
  public void testGetCategories() {
    final Categories categories = datastoreService.getCategories();
    Assert.assertNotNull("Categories null", categories);
    Assert.assertFalse("Categories empty", categories.getCategories().isEmpty());
    Assert.assertEquals("Categories total doesn't match", categories.getTotal(), categories.getCategories().size());
  }

  @Test
  public void testGetDevelopers() {
    final Developers developers = datastoreService.getDevelopers();
    Assert.assertNotNull("Developers null", developers);
    Assert.assertFalse("Developers empty", developers.getDevelopers().isEmpty());
    Assert.assertEquals("Developers total doesn't match", developers.getTotal(), developers.getDevelopers().size());
  }

  @Test
  public void testGetLabels() {
    final Labels labels = datastoreService.getLabels();
    Assert.assertNotNull("Labels null", labels);
    Assert.assertFalse("Labels empty", labels.getLabels().isEmpty());
    Assert.assertEquals("Labels total doesn't match", labels.getTotal(), labels.getLabels().size());
  }


}
