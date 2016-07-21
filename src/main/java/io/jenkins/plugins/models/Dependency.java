package io.jenkins.plugins.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Dependency {

  @JsonProperty("name")
  private String name;

  @JsonProperty("optional")
  private boolean optional;

  @JsonProperty("version")
  private String version;

  public Dependency() {
  }

  public Dependency(String name, boolean optional, String version) {
    this.name = name;
    this.optional = optional;
    this.version = version;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isOptional() {
    return optional;
  }

  public void setOptional(boolean optional) {
    this.optional = optional;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }
}
