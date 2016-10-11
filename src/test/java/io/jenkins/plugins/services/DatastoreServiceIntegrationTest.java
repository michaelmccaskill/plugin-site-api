package io.jenkins.plugins.services;

import io.jenkins.plugins.models.*;
import io.jenkins.plugins.services.impl.ElasticsearchDatastoreService;
import io.jenkins.plugins.services.impl.HttpClientWikiService;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import javax.inject.Singleton;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ScheduledExecutorService;

public class DatastoreServiceIntegrationTest {

  private static ServiceLocator locator;
  private static DatastoreService datastoreService;

  private static ScheduledExecutorService mockScheduledExecutorService;

  // BeforeClass because there's no reason to start/stop Elasticsearch for every test when
  // all our operations are read-only
  @BeforeClass
  public static void setUp() throws Exception {
    mockScheduledExecutorService = Mockito.mock(ScheduledExecutorService.class);
    locator  = ServiceLocatorUtilities.bind(
      new io.jenkins.plugins.datastore.Binder(),
      new AbstractBinder() {
        @Override
        protected void configure() {
          bind(mockScheduledExecutorService.getClass()).to(ScheduledExecutorService.class).in(Singleton.class);
          bind(MockConfigurationService.class).to(ConfigurationService.class).in(Singleton.class);
          bind(ElasticsearchDatastoreService.class).to(DatastoreService.class).in(Singleton.class);
          bind(HttpClientWikiService.class).to(WikiService.class).in(Singleton.class);
        }
      });
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
    Assert.assertFalse("Maintainers are empty", plugin.getMaintainers().isEmpty());
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
    final Plugins plugins = datastoreService.search(new SearchOptions("git", SortBy.INSTALLED, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), null, 50, 1));
    Assert.assertNotNull("Search for 'git' sort by installs is null", plugins);
    Assert.assertTrue("Should return multiple results", plugins.getTotal() > 1);
    Assert.assertTrue("SortBy.INSTALLED not correct", plugins.getPlugins().get(0).getStats().getCurrentInstalls() > plugins.getPlugins().get(1).getStats().getCurrentInstalls());
  }

  @Test
  public void testSearchSortByName() {
    final Plugins plugins = datastoreService.search(new SearchOptions("git", SortBy.NAME, Collections.emptyList(),  Collections.emptyList(), Collections.emptyList(), null, 50, 1));
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
  public void testSearchSortByTitle() {
    final Plugins plugins = datastoreService.search(new SearchOptions("git", SortBy.TITLE ,Collections.emptyList(),  Collections.emptyList(), Collections.emptyList(), null, 50, 1));
    Assert.assertNotNull("Search for 'git' sort by title is null", plugins);
    Assert.assertTrue("Should return multiple results", plugins.getTotal() > 1);
    Assert.assertTrue("SortBy.TITLE not correct", plugins.getPlugins().get(0).getTitle().compareTo(plugins.getPlugins().get(1).getTitle()) < 0);
  }

  @Test
  public void testSearchSortByTrend() {
    final Plugins plugins = datastoreService.search(new SearchOptions("git", SortBy.TREND, Collections.emptyList(),  Collections.emptyList(), Collections.emptyList(), null, 50, 1));
    Assert.assertNotNull("Search for 'git' sort by trend is null", plugins);
    Assert.assertTrue("Should return multiple results", plugins.getTotal() > 1);
    Assert.assertTrue("SortBy.TREND not correct", plugins.getPlugins().get(0).getStats().getTrend() > plugins.getPlugins().get(1).getStats().getTrend());
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
  public void testSearchMaintainers() {
    final Plugins plugins = datastoreService.search(new SearchOptions(null, null, Collections.emptyList(), Collections.emptyList(), Arrays.asList("Kohsuke Kawaguchi"), null, 50, 1));
    Assert.assertNotNull("Search for maintainers 'Kohsuke Kawaguchi' is null", plugins);
    for (Plugin plugin : plugins.getPlugins()) {
      for (Maintainer maintainer : plugin.getMaintainers()) {
        if (maintainer.getName().equalsIgnoreCase("Kohsuke Kawaguchi")) {
          return;
        }
      }
    }
    Assert.fail("Didn't find plugins with maintainers 'Kohsuke Kawaguchi'");
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
    Assert.assertEquals("Categories limit doesn't match", categories.getLimit(), categories.getCategories().size());
  }

  @Test
  public void testGetMaintainers() {
    final Maintainers maintainers = datastoreService.getMaintainers();
    Assert.assertNotNull("Maintainers null", maintainers);
    Assert.assertFalse("Maintainers empty", maintainers.getMaintainers().isEmpty());
    Assert.assertEquals("Maintainers limit doesn't match", maintainers.getLimit(), maintainers.getMaintainers().size());
  }

  @Test
  public void testGetLabels() {
    final Labels labels = datastoreService.getLabels();
    Assert.assertNotNull("Labels null", labels);
    Assert.assertFalse("Labels empty", labels.getLabels().isEmpty());
    Assert.assertEquals("Labels limit doesn't match", labels.getLimit(), labels.getLabels().size());
  }

  @Test
  public void testGetVersions() {
    final Versions versions = datastoreService.getVersions();
    Assert.assertNotNull("Versions null", versions);
    Assert.assertFalse("Versions empty", versions.getVersions().isEmpty());
    Assert.assertEquals("Versions limit doesn't match", versions.getLimit(), versions.getVersions().size());
  }


}
