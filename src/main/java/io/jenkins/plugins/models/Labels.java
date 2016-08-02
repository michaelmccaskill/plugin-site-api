package io.jenkins.plugins.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Labels {

  @JsonProperty("labels")
  private List<Label> labels;

  @JsonProperty("limit")
  private int limit;

  public Labels() {
  }

  public Labels(List<Label> labels) {
    this.labels = labels;
    this.limit = labels.size();
  }

  public List<Label> getLabels() {
    return labels;
  }

  public void setLabels(List<Label> labels) {
    this.labels = labels;
  }

  public int getLimit() {
    return limit;
  }

  public void setLimit(int limit) {
    this.limit = limit;
  }
}
