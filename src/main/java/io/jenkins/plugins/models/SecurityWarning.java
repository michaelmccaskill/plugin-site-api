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

  @JsonProperty("active")
  private boolean active;

  @JsonProperty
  private List<SecurityWarningVersion> versions;

  public SecurityWarning() {
  }

  public SecurityWarning(String id, String message, String url, boolean active, List<SecurityWarningVersion> versions) {
    this.id = id;
    this.message = message;
    this.url = url;
    this.active = active;
    this.versions = versions;
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

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public List<SecurityWarningVersion> getVersions() {
    return versions;
  }

  public void setVersions(List<SecurityWarningVersion> versions) {
    this.versions = versions;
  }

}
