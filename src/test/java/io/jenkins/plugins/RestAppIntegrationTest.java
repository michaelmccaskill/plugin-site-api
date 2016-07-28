package io.jenkins.plugins;

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
