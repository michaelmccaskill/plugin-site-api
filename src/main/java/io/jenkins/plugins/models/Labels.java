package io.jenkins.plugins.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Labels {

  @JsonProperty("labels")
  private List<Label> labels;

  @JsonProperty("total")
  private int total;

  public Labels() {
  }

  public Labels(List<Label> labels) {
    this.labels = labels;
    this.total = labels.size();
  }

  public List<Label> getLabels() {
    return labels;
  }

  public void setLabels(List<Label> labels) {
    this.labels = labels;
  }

  public int getTotal() {
    return total;
  }

  public void setTotal(int total) {
    this.total = total;
  }
}
