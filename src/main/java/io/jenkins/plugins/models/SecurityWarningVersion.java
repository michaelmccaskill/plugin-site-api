package io.jenkins.plugins.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SecurityWarningVersion {

  @JsonProperty("lastVersion")
  private String lastVersion;

  @JsonProperty("applyToCurrentVersion")
  private boolean applyToCurrentVersion;

  public SecurityWarningVersion() {
  }

  public SecurityWarningVersion(String lastVersion, boolean applyToCurrentVersion) {
    this.lastVersion = lastVersion;
    this.applyToCurrentVersion = applyToCurrentVersion;
  }

  public String getLastVersion() {
    return lastVersion;
  }

  public void setLastVersion(String lastVersion) {
    this.lastVersion = lastVersion;
  }

  public boolean isApplyToCurrentVersion() {
    return applyToCurrentVersion;
  }

  public void setApplyToCurrentVersion(boolean applyToCurrentVersion) {
    this.applyToCurrentVersion = applyToCurrentVersion;
  }
}
