package io.jenkins.plugins.services;

public class DatastoreException extends RuntimeException {

  public DatastoreException(String msg, Throwable cause) {
    super(msg, cause);
  }

}
