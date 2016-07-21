package io.jenkins.plugins.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InstallationPercentage {

  @JsonProperty("timestamp")
  private long timestamp;

  @JsonProperty("percentage")
  private double percentage;

  public InstallationPercentage() {
  }

  public InstallationPercentage(long timestamp, double percentage) {
    this.timestamp = timestamp;
    this.percentage = percentage;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  public double getPercentage() {
    return percentage;
  }

  public void setPercentage(double percentage) {
    this.percentage = percentage;
  }
}
