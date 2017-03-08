package io.jenkins.plugins.services;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>Container for search options</p>
 */
public class SearchOptions {

  public static class Builder {
    private String query;
    private SortBy sortBy;
    private Set<String> categories;
    private Set<String> labels;
    private Set<String> maintainers;
    private String core;
    private Integer limit;
    private Integer page;
    private Boolean onlyNew;

    public Builder() {
      this.query = null;
      this.sortBy = SortBy.RELEVANCE;
      this.categories = new HashSet<>();
      this.labels = new HashSet<>();
      this.maintainers = new HashSet<>();
      this.core = null;
      this.limit = 50;
      this.page = 1;
      this.onlyNew = false;
    }

    public Builder withQuery(String query) {
      this.query = StringUtils.trimToNull(query);
      return this;
    }

    public Builder withSortBy(SortBy sortBy) {
      this.sortBy = ObjectUtils.defaultIfNull(sortBy, SortBy.RELEVANCE);
      return this;
    }

    public Builder withCategories(String... categories) {
      for (String category : categories) {
        if (StringUtils.isNotBlank(category)) {
          this.categories.add(category);
        }
      }
      return this;
    }

    public Builder withCategories(Set<String> categories) {
      this.categories = categories != null
        ? categories.stream().filter(StringUtils::isNotBlank).collect(Collectors.toSet())
        : Collections.emptySet();
      return this;
    }

    public Builder withLabels(String... labels) {
      for (String label : labels) {
        if (StringUtils.isNotBlank(label)) {
          this.labels.add(label);
        }
      }
      return this;
    }

    public Builder withLabels(Set<String> labels) {
      this.labels = labels != null
        ? labels.stream().filter(StringUtils::isNotBlank).collect(Collectors.toSet())
        : Collections.emptySet();
      return this;
    }

    public Builder withMaintainers(String... maintainers) {
      for (String maintainer : maintainers) {
        if (StringUtils.isNotBlank(maintainer)) {
          this.maintainers.add(maintainer);
        }
      }
      return this;
    }

    public Builder withMaintainers(Set<String> maintainers) {
      this.maintainers = maintainers != null
        ? maintainers.stream().filter(StringUtils::isNotBlank).collect(Collectors.toSet())
        : Collections.emptySet();
      return this;
    }

    public Builder withCore(String core) {
      this.core = StringUtils.trimToNull(core);
      return this;
    }

    public Builder withLimit(Integer limit) {
      this.limit = ObjectUtils.defaultIfNull(limit, 50);
      return this;
    }

    public Builder withPage(Integer page) {
      this.page = ObjectUtils.defaultIfNull(page, 1);
      return this;
    }

    public Builder withOnlyNew(Boolean onlyNew) {
      this.onlyNew = BooleanUtils.toBoolean(onlyNew);
      return this;
    }

    public SearchOptions build() {
      return new SearchOptions(
        query, sortBy, categories, labels, maintainers, core, limit, page, onlyNew
      );
    }

  }

  private String query;
  private SortBy sortBy;
  private Set<String> categories;
  private Set<String> labels;
  private Set<String> maintainers;
  private String core;
  private Integer limit;
  private Integer page;
  private Boolean onlyNew;

  private SearchOptions(String query, SortBy sortBy, Set<String> categories, Set<String> labels, Set<String> maintainers,
                       String core, Integer limit, Integer page, Boolean onlyNew) {
    this.query = query;
    this.sortBy = sortBy;
    this.categories = categories;
    this.labels = labels;
    this.maintainers = maintainers;
    this.core = core;
    this.limit = limit;
    this.page = page;
    this.onlyNew = onlyNew;
  }

  public String getQuery() {
    return query;
  }

  public SortBy getSortBy() {
    return sortBy;
  }

  public Set<String> getCategories() {
    return categories;
  }

  public Set<String> getLabels() {
    return labels;
  }

  public Set<String> getMaintainers() {
    return maintainers;
  }

  public String getCore() {
    return core;
  }

  public Integer getLimit() {
    return limit;
  }

  public Integer getPage() {
    return page;
  }

  public Boolean isOnlyNew() {
    return onlyNew;
  }

  public Boolean hasFilters() {
    return !getMaintainers().isEmpty() || !getCategories().isEmpty()
      || getCore() != null || !getLabels().isEmpty()
      || onlyNew;
  }
}
