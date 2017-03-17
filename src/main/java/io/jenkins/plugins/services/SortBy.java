package io.jenkins.plugins.services;

/**
 * <p>Enum specifying sorting search results</p>
 */
public enum SortBy {

  FIRST_RELEASE,
  INSTALLED,
  NAME,
  RELEVANCE,
  TITLE,
  TREND,
  UPDATED;

  public String value() {
    return name().toLowerCase();
  }

  public static SortBy fromString(String s) {
    return valueOf(s.toUpperCase());
  }

}
