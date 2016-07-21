package io.jenkins.plugins.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InstallationPercentageVersion {

  @JsonProperty("version")
  private String version;

  @JsonProperty("percentage")
  private double percentage;

  public InstallationPercentageVersion() {
  }

  public InstallationPercentageVersion(String version, double percentage) {
    this.version = version;
    this.percentage = percentage;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public double getPercentage() {
    return percentage;
  }

  public void setPercentage(double percentage) {
    this.percentage = percentage;
  }
}
