package io.jenkins.plugins.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Plugin {

  @JsonProperty("buildDate")
  private String buildDate; // date - MMM dd, YYYY

  @JsonProperty("categories")
  private List<String> categories;

  @JsonProperty("dependencies")
  private List<Dependency> dependencies;

  @JsonProperty("developers")
  private List<Developer> developers;

  @JsonProperty("excerpt")
  private String excerpt;

  @JsonProperty("gav")
  private String gav;

  @JsonProperty("labels")
  private List<String> labels;

  @JsonProperty("name")
  private String name;

  @JsonProperty("previousTimestamp")
  private String previousTimestamp; // ISO-8601 date 2016-07-21T17:40:28Z

  @JsonProperty("previousVersion")
  private String previousVersion;

  @JsonProperty("releaseTimetamp")
  private String releaseTimestamp;  // ISO-8601 date 2016-07-21T17:40:28Z

  @JsonProperty("requiredCore")
  private String requiredCore;

  @JsonProperty("scm")
  private String scm;

  @JsonProperty("sha1")
  private String sha1;

  @JsonProperty("stats")
  private Stats stats;

  @JsonProperty("title")
  private String title;

  @JsonProperty("url")
  private String url;

  @JsonProperty("version")
  private String version;

  @JsonProperty("wiki")
  private String wiki;

  public Plugin() {
  }

  public Plugin(String buildDate, List<String> categories, List<Dependency> dependencies, List<Developer> developers,
                String excerpt, String gav, List<String> labels, String name, String previousTimestamp,
                String previousVersion, String releaseTimestamp, String requiredCore, String scm, String sha1,
                Stats stats, String title, String url, String version, String wiki) {
    this.buildDate = buildDate;
    this.categories = categories;
    this.dependencies = dependencies;
    this.developers = developers;
    this.excerpt = excerpt;
    this.gav = gav;
    this.labels = labels;
    this.name = name;
    this.previousTimestamp = previousTimestamp;
    this.previousVersion = previousVersion;
    this.releaseTimestamp = releaseTimestamp;
    this.requiredCore = requiredCore;
    this.scm = scm;
    this.sha1 = sha1;
    this.stats = stats;
    this.title = title;
    this.url = url;
    this.version = version;
    this.wiki = wiki;
  }

  public String getBuildDate() {
    return buildDate;
  }

  public void setBuildDate(String buildDate) {
    this.buildDate = buildDate;
  }

  public List<String> getCategories() {
    return categories;
  }

  public void setCategories(List<String> categories) {
    this.categories = categories;
  }

  public List<Dependency> getDependencies() {
    return dependencies;
  }

  public void setDependencies(List<Dependency> dependencies) {
    this.dependencies = dependencies;
  }

  public List<Developer> getDevelopers() {
    return developers;
  }

  public void setDevelopers(List<Developer> developers) {
    this.developers = developers;
  }

  public String getExcerpt() {
    return excerpt;
  }

  public void setExcerpt(String excerpt) {
    this.excerpt = excerpt;
  }

  public String getGav() {
    return gav;
  }

  public void setGav(String gav) {
    this.gav = gav;
  }

  public List<String> getLabels() {
    return labels;
  }

  public void setLabels(List<String> labels) {
    this.labels = labels;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getPreviousTimestamp() {
    return previousTimestamp;
  }

  public void setPreviousTimestamp(String previousTimestamp) {
    this.previousTimestamp = previousTimestamp;
  }

  public String getPreviousVersion() {
    return previousVersion;
  }

  public void setPreviousVersion(String previousVersion) {
    this.previousVersion = previousVersion;
  }

  public String getReleaseTimestamp() {
    return releaseTimestamp;
  }

  public void setReleaseTimestamp(String releaseTimestamp) {
    this.releaseTimestamp = releaseTimestamp;
  }

  public String getRequiredCore() {
    return requiredCore;
  }

  public void setRequiredCore(String requiredCore) {
    this.requiredCore = requiredCore;
  }

  public String getScm() {
    return scm;
  }

  public void setScm(String scm) {
    this.scm = scm;
  }

  public String getSha1() {
    return sha1;
  }

  public void setSha1(String sha1) {
    this.sha1 = sha1;
  }

  public Stats getStats() {
    return stats;
  }

  public void setStats(Stats stats) {
    this.stats = stats;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getWiki() {
    return wiki;
  }

  public void setWiki(String wiki) {
    this.wiki = wiki;
  }
}
