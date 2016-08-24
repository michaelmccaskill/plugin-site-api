package io.jenkins.plugins.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Maintainers {

  @JsonProperty("maintainers")
  private List<String> maintainers;

  @JsonProperty("limit")
  private int limit;

  public Maintainers() {
  }

  public Maintainers(List<String> maintainers) {
    this.maintainers = maintainers;
    this.limit = maintainers.size();
  }

  public List<String> getMaintainers() {
    return maintainers;
  }

  public void setMaintainers(List<String> maintainers) {
    this.maintainers = maintainers;
  }

  public int getLimit() {
    return limit;
  }

  public void setLimit(int limit) {
    this.limit = limit;
  }
}
