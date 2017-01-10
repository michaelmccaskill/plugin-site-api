package io.jenkins.plugins.utils;

import org.apache.commons.codec.digest.DigestUtils;
import org.elasticsearch.Version;

import java.io.IOException;

public class VersionUtils {

  public static String getMappingVersion() {
    try {
      return DigestUtils.sha256Hex(VersionUtils.class.getClassLoader().getResourceAsStream("elasticsearch/mappings/plugins.json"));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static String getElasticsearchVersion() {
    return DigestUtils.sha256Hex(Version.CURRENT.toString());
  }

}
