package io.hhplus.tdd.point.exceptions;

public class InsufficientPointsException extends RuntimeException {
  public InsufficientPointsException() {
    super("Insufficient points");
  }
}
