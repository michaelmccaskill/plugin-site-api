package io.jenkins.plugins.datastore;

import io.jenkins.plugins.models.Categories;
import io.jenkins.plugins.models.Labels;
import io.jenkins.plugins.models.Plugin;
import io.jenkins.plugins.models.Plugins;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

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
    final Plugins plugins = datastoreService.search("git", null, Collections.emptyList(), Collections.emptyList(), null, 50, 1);
    Assert.assertNotNull("Search for 'git' null'", plugins);
    Assert.assertTrue("Should return multiple results", plugins.getTotal() > 1);
  }

  @Test
  public void testGetCategories() {
    final Categories categories = datastoreService.getCategories();
    Assert.assertNotNull("Categories null'", categories);
    Assert.assertFalse("Categories empty", categories.getCategories().isEmpty());
  }

  @Test
  public void testGetLabels() {
    final Labels labels = datastoreService.getLabels();
    Assert.assertNotNull("Labels null'", labels);
    Assert.assertFalse("Labels empty", labels.getLabels().isEmpty());
  }


}
