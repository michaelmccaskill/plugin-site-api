package io.jenkins.plugins.services;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SearchOptions {

  private String query;
  private SortBy sortBy;
  private List<String> categories;
  private List<String> labels;
  private List<String> authors;
  private String core;
  private Integer limit;
  private Integer page;

  public SearchOptions(String query, SortBy sortBy, List<String> categories, List<String> labels, List<String> authors, String core, Integer limit, Integer page) {
    setQuery(query);
    setSortBy(sortBy);
    setCategories(categories);
    setLabels(labels);
    setAuthors(authors);
    setCore(core);
    setLimit(limit);
    setPage(page);
  }

  public SearchOptions(String query, SortBy sortBy, String categories, String labels, String authors, String core, Integer limit, Integer page) {
    setQuery(query);
    setSortBy(sortBy);
    setCategories(categories != null && !categories.trim().isEmpty() ? Arrays.asList(categories.split(",")) : Collections.emptyList());
    setLabels(labels != null && !labels.trim().isEmpty() ? Arrays.asList(labels.split(",")) : Collections.emptyList());
    setAuthors(authors != null && !authors.trim().isEmpty() ? Arrays.asList(authors.split(",")) : Collections.emptyList());
    setCore(core);
    setLimit(limit);
    setPage(page);
  }

  public String getQuery() {
    return query;
  }

  public void setQuery(String query) {
    this.query = query != null && !query.trim().isEmpty() ? query.trim() : null;
  }

  public SortBy getSortBy() {
    return sortBy;
  }

  public void setSortBy(SortBy sortBy) {
    this.sortBy = sortBy != null ? sortBy : SortBy.RELEVANCE;
  }

  public List<String> getCategories() {
    return categories;
  }

  public void setCategories(List<String> categories) {
    this.categories = categories != null ? categories : Collections.emptyList();
  }

  public List<String> getLabels() {
    return labels;
  }

  public void setLabels(List<String> labels) {
    this.labels = labels != null ? labels : Collections.emptyList();
  }

  public List<String> getAuthors() {
    return authors;
  }

  public void setAuthors(List<String> authors) {
    this.authors = authors != null ? authors : Collections.emptyList();
  }

  public String getCore() {
    return core;
  }

  public void setCore(String core) {
    this.core = core != null && !core.trim().isEmpty() ? core.trim() : null;
  }

  public Integer getLimit() {
    return limit;
  }

  public void setLimit(Integer limit) {
    this.limit = limit != null ? limit : 50;
  }

  public Integer getPage() {
    return page;
  }

  public void setPage(Integer page) {
    this.page = page != null ? page : 1;
  }
}
