package io.jenkins.plugins.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Plugins {

  @JsonProperty("plugins")
  private List<Plugin> plugins;

  @JsonProperty("page")
  private int page;

  @JsonProperty("pages")
  private long pages;

  @JsonProperty("total")
  private long total;

  @JsonProperty("limit")
  private int limit;

  public Plugins() {
  }

  public Plugins(List<Plugin> plugins, int page, long pages, long total, int limit) {
    this.plugins = plugins;
    this.page = page;
    this.pages = pages;
    this.total = total;
    this.limit = limit;
  }

  public List<Plugin> getPlugins() {
    return plugins;
  }

  public void setPlugins(List<Plugin> plugins) {
    this.plugins = plugins;
  }

  public int getPage() {
    return page;
  }

  public void setPage(int page) {
    this.page = page;
  }

  public long getPages() {
    return pages;
  }

  public void setPages(long pages) {
    this.pages = pages;
  }

  public long getTotal() {
    return total;
  }

  public void setTotal(long total) {
    this.total = total;
  }

  public int getLimit() {
    return limit;
  }

  public void setLimit(int limit) {
    this.limit = limit;
  }
}
