package io.hhplus.tdd;

import io.hhplus.tdd.point.exceptions.InsufficientPointsException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
class ApiControllerAdvice extends ResponseEntityExceptionHandler {
  @ExceptionHandler(value = Exception.class)
  public ResponseEntity<ErrorResponse> handleException(Exception e) {
    if (e instanceof IllegalArgumentException)
      return ResponseEntity.status(400).body(new ErrorResponse("400", e.getMessage()));
    if (e instanceof InsufficientPointsException)
      return ResponseEntity.status(400).body(new ErrorResponse("400", e.getMessage()));

    return ResponseEntity.status(500).body(new ErrorResponse("500", "에러가 발생했습니다."));
  }
}
