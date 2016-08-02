package io.jenkins.plugins.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Categories {

  @JsonProperty("categories")
  private List<Category> categories;

  @JsonProperty("limit")
  private int limit;

  public Categories() {
  }

  public Categories(List<Category> categories) {
    this.categories = categories;
    this.limit = categories.size();
  }

  public List<Category> getCategories() {
    return categories;
  }

  public void setCategories(List<Category> categories) {
    this.categories = categories;
  }

  public int getLimit() {
    return limit;
  }

  public void setLimit(int limit) {
    this.limit = limit;
  }
}
