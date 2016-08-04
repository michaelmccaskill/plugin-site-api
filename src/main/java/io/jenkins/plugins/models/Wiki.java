package io.jenkins.plugins.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Wiki {

  @JsonProperty("content")
  @JsonIgnoreProperties(allowSetters = false)
  private String content;

  @JsonProperty("url")
  private String url;

  public Wiki() {
  }

  public Wiki(String content, String url) {
    this.content = content;
    this.url = url;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }
}
