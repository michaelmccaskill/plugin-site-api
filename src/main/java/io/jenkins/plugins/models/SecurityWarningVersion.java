package io.jenkins.plugins.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SecurityWarningVersion {

  @JsonProperty("firstVersion")
  private String firstVersion;

  @JsonProperty("lastVersion")
  private String lastVersion;

  public SecurityWarningVersion() {
  }

  public SecurityWarningVersion(String firstVersion, String lastVersion) {
    this.firstVersion = firstVersion;
    this.lastVersion = lastVersion;
  }

  public String getFirstVersion() {
    return firstVersion;
  }

  public void setFirstVersion(String firstVersion) {
    this.firstVersion = firstVersion;
  }

  public String getLastVersion() {
    return lastVersion;
  }

  public void setLastVersion(String lastVersion) {
    this.lastVersion = lastVersion;
  }


}
