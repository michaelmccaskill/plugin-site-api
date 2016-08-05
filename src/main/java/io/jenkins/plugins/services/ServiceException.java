package io.jenkins.plugins.services;

public class ServiceException extends RuntimeException {

  public ServiceException(String msg, Throwable cause) {
    super(msg, cause);
  }

}
