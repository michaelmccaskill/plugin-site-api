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

  @JsonProperty("mappingVersion")
  private String mappingVersion;

  @JsonProperty("elasticsearchVersion")
  private String elasticsearchVersion;

  public GeneratedPluginData() {
  }

  public GeneratedPluginData(List<Plugin> plugins, String mappingVersion, String elasticsearchVersion) {
    this.plugins = plugins;
    this.createdAt = LocalDateTime.now();
    this.mappingVersion = mappingVersion;
    this.elasticsearchVersion = elasticsearchVersion;
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

  public String getMappingVersion() {
    return mappingVersion;
  }

  public void setMappingVersion(String mappingVersion) {
    this.mappingVersion = mappingVersion;
  }

  public String getElasticsearchVersion() {
    return elasticsearchVersion;
  }

  public void setElasticsearchVersion(String elasticsearchVersion) {
    this.elasticsearchVersion = elasticsearchVersion;
  }
}
