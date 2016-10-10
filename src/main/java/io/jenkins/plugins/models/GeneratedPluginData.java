package io.jenkins.plugins.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import java.time.LocalDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GeneratedPluginData {

  @JsonProperty("plugins")
  private List<Plugin> plugins;

  @JsonProperty("createdAt")
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SS'Z'")
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  private LocalDateTime createdAt;

  public GeneratedPluginData() {
  }

  public GeneratedPluginData(List<Plugin> plugins) {
    this.plugins = plugins;
    this.createdAt = LocalDateTime.now();
  }

  public List<Plugin> getPlugins() {
    return plugins;
  }

  public void setPlugins(List<Plugin> plugins) {
    this.plugins = plugins;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

}
