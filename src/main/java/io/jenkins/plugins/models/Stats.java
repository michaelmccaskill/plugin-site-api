package io.jenkins.plugins.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Stats {

  @JsonProperty("installations")
  private List<Installation> installations;

  @JsonProperty("installationsPercentage")
  private List<InstallationPercentage> installationsPercentage;

  @JsonProperty("installationsPerVersion")
  private List<InstallationVersion> installationsPerVersion;

  @JsonProperty("installationsPercentagePerVersion")
  private List<InstallationPercentageVersion> installationsPercentagePerVersion;

  @JsonProperty("lifetime")
  private int lifetime;

  public Stats() {
  }

  public Stats(List<Installation> installations, List<InstallationPercentage> installationsPercentage, List<InstallationVersion> installationsPerVersion, List<InstallationPercentageVersion> installationsPercentagePerVersion, int lifetime) {
    this.installations = installations;
    this.installationsPercentage = installationsPercentage;
    this.installationsPerVersion = installationsPerVersion;
    this.installationsPercentagePerVersion = installationsPercentagePerVersion;
    this.lifetime = lifetime;
  }

  public List<Installation> getInstallations() {
    return installations;
  }

  public void setInstallations(List<Installation> installations) {
    this.installations = installations;
  }

  public List<InstallationPercentage> getInstallationsPercentage() {
    return installationsPercentage;
  }

  public void setInstallationsPercentage(List<InstallationPercentage> installationsPercentage) {
    this.installationsPercentage = installationsPercentage;
  }

  public List<InstallationVersion> getInstallationsPerVersion() {
    return installationsPerVersion;
  }

  public void setInstallationsPerVersion(List<InstallationVersion> installationsPerVersion) {
    this.installationsPerVersion = installationsPerVersion;
  }

  public List<InstallationPercentageVersion> getInstallationsPercentagePerVersion() {
    return installationsPercentagePerVersion;
  }

  public void setInstallationsPercentagePerVersion(List<InstallationPercentageVersion> installationsPercentagePerVersion) {
    this.installationsPercentagePerVersion = installationsPercentagePerVersion;
  }

  public int getLifetime() {
    return lifetime;
  }

  public void setLifetime(int lifetime) {
    this.lifetime = lifetime;
  }
}
