package io.jenkins.plugins.services;

/**
 * <p>Service tier exception</p>
 */
public class ServiceException extends RuntimeException {

  public ServiceException(String msg, Throwable cause) {
    super(msg, cause);
  }

}
