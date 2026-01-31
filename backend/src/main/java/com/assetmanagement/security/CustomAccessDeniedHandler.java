package com.assetmanagement.security;

import com.assetmanagement.exception.ErrorDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public void handle(HttpServletRequest request, HttpServletResponse response,
                     AccessDeniedException accessDeniedException) throws IOException {
    response.setStatus(HttpStatus.FORBIDDEN.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

    ErrorDetails errorDetails = new ErrorDetails(
            LocalDateTime.now(),
            HttpStatus.FORBIDDEN.getReasonPhrase(),
            "Access denied: insufficient permissions"
    );

    response.getOutputStream().write(objectMapper.writeValueAsBytes(errorDetails));
  }
}