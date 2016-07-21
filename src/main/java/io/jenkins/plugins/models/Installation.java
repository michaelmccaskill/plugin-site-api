package io.jenkins.plugins.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Installation {

  @JsonProperty("timestamp")
  private long timestamp;

  @JsonProperty("total")
  private int  total;

  public Installation() {
  }

  public Installation(long timestamp, int total) {
    this.timestamp = timestamp;
    this.total = total;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  public int getTotal() {
    return total;
  }

  public void setTotal(int total) {
    this.total = total;
  }
}
