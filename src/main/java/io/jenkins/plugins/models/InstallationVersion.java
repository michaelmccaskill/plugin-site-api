package io.jenkins.plugins.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InstallationVersion {

  @JsonProperty("version")
  private String version;

  @JsonProperty("total")
  private int total;

  public InstallationVersion() {
  }

  public InstallationVersion(String version, int total) {
    this.version = version;
    this.total = total;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public int getTotal() {
    return total;
  }

  public void setTotal(int total) {
    this.total = total;
  }
}
