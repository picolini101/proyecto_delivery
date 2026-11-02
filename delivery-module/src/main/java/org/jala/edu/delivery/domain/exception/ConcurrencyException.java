package org.jala.edu.delivery.domain.exception;

public class ConcurrencyException extends RuntimeException{
  public ConcurrencyException(String message) {
    super(message);
  }
}
