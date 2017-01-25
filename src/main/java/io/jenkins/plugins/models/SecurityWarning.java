package io.jenkins.plugins.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SecurityWarning {

  @JsonProperty("id")
  private String id;

  @JsonProperty("message")
  private String message;

  @JsonProperty("url")
  private String url;

  @JsonProperty("warnings")
  private List<SecurityWarningVersion> versions;

  @JsonProperty("applyToCurrentVersion")
  private boolean applyToCurrentVersion;

  public SecurityWarning() {
  }

  public SecurityWarning(String id, String message, String url, List<SecurityWarningVersion> versions, boolean applyToCurrentVersion) {
    this.id = id;
    this.message = message;
    this.url = url;
    this.versions = versions;
    this.applyToCurrentVersion = applyToCurrentVersion;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public List<SecurityWarningVersion> getVersions() {
    return versions;
  }

  public void setVersions(List<SecurityWarningVersion> versions) {
    this.versions = versions;
  }

  public boolean isApplyToCurrentVersion() {
    return applyToCurrentVersion;
  }

  public void setApplyToCurrentVersion(boolean applyToCurrentVersion) {
    this.applyToCurrentVersion = applyToCurrentVersion;
  }

}
