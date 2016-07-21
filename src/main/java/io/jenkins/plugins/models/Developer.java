package io.jenkins.plugins.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Developer {

  @JsonProperty("developerId")
  private String developerId;

  @JsonProperty("name")
  private String name;

  @JsonProperty("email")
  private String email;

  public Developer() {
  }

  public Developer(String developerId, String name, String email) {
    this.developerId = developerId;
    this.name = name;
    this.email = email;
  }

  public String getDeveloperId() {
    return developerId;
  }

  public void setDeveloperId(String developerId) {
    this.developerId = developerId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }
}
