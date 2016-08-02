package io.jenkins.plugins.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Categories {

  @JsonProperty("categories")
  private List<Category> categories;

  @JsonProperty("total")
  private int total;

  public Categories() {
  }

  public Categories(List<Category> categories) {
    this.categories = categories;
    this.total = categories.size();
  }

  public List<Category> getCategories() {
    return categories;
  }

  public void setCategories(List<Category> categories) {
    this.categories = categories;
  }

  public int getTotal() {
    return total;
  }

  public void setTotal(int total) {
    this.total = total;
  }
}
