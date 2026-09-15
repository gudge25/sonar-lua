package org.sonar.api.utils;

public class SonarException extends RuntimeException {
  public SonarException() {
    super();
  }

  public SonarException(String message) {
    super(message);
  }

  public SonarException(String message, Throwable cause) {
    super(message, cause);
  }

  public SonarException(Throwable cause) {
    super(cause);
  }
}
