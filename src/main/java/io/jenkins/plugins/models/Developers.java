package io.jenkins.plugins.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Developers {

  @JsonProperty("developers")
  private List<String> developers;

  @JsonProperty("limit")
  private int limit;

  public Developers() {
  }

  public Developers(List<String> developers) {
    this.developers = developers;
    this.limit = developers.size();
  }

  public List<String> getDevelopers() {
    return developers;
  }

  public void setDevelopers(List<String> developers) {
    this.developers = developers;
  }

  public int getLimit() {
    return limit;
  }

  public void setLimit(int limit) {
    this.limit = limit;
  }
}
