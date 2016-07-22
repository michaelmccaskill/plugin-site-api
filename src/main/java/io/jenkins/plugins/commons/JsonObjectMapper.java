package io.jenkins.plugins.commons;

import com.fasterxml.jackson.databind.ObjectMapper;

public final class JsonObjectMapper {

  private static final ObjectMapper mapper = createObjectMapper();

  public static ObjectMapper getObjectMapper() {
    return mapper;
  }

  private static ObjectMapper createObjectMapper() {
    final ObjectMapper objectMapper = new ObjectMapper();
    return objectMapper;
  }

}
