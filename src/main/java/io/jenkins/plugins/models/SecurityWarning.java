package io.jenkins.plugins.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

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

  @JsonProperty("firstVersion")
  private String firstVersion;

  @JsonProperty("lastVersion")
  private String lastVersion;

  public SecurityWarning() {
  }

  public SecurityWarning(String id, String message, String url, boolean active, String firstVersion, String lastVersion) {
    this.id = id;
    this.message = message;
    this.url = url;
    this.active = active;
    this.firstVersion = firstVersion;
    this.lastVersion = lastVersion;
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
