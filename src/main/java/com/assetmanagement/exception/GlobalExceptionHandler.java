package com.assetmanagement.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import java.time.LocalDateTime;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  @ExceptionHandler(ApplicationException.class)
  public ResponseEntity<?> handleException(ApplicationException e) {
    log.error("Session API exception raised: {}", e.getMessage());
    return ResponseEntity.status(e.getHttpStatus())
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorDetails(
                LocalDateTime.now(), e.getHttpStatus().getReasonPhrase(), e.getMessage()));
  }
}
