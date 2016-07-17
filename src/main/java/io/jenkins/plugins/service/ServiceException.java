package io.jenkins.plugins.service;

public class ServiceException extends RuntimeException {

  public ServiceException(String msg, Throwable cause) {
    super(msg, cause);
  }

}
