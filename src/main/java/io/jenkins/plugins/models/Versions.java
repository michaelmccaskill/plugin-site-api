package io.jenkins.plugins.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Versions {

  @JsonProperty("versions")
  private List<String> versions;

  @JsonProperty("limit")
  private int limit;

  public Versions() {
  }

  public Versions(List<String> versions) {
    this.versions = versions;
    this.limit = versions.size();
  }

  public List<String> getVersions() {
    return versions;
  }

  public void setVersions(List<String> versions) {
    this.versions = versions;
  }

  public int getLimit() {
    return limit;
  }

  public void setLimit(int limit) {
    this.limit = limit;
  }
}
