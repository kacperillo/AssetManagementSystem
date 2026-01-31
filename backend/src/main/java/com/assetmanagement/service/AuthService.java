package com.assetmanagement.service;

import com.assetmanagement.dto.request.ChangePasswordRequest;
import com.assetmanagement.dto.request.LoginRequest;
import com.assetmanagement.dto.response.LoginResponse;
import com.assetmanagement.exception.ApplicationException;
import com.assetmanagement.model.Employee;
import com.assetmanagement.repository.EmployeeRepository;
import com.assetmanagement.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final AuthenticationManager authenticationManager;
  private final JwtUtil jwtUtil;
  private final EmployeeRepository employeeRepository;
  private final PasswordEncoder passwordEncoder;

  public LoginResponse login(LoginRequest request) {
    try {
      authenticationManager.authenticate(
              new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
      );
    } catch (AuthenticationException e) {
      throw new ApplicationException(HttpStatus.UNAUTHORIZED, "Nieprawidłowy email lub hasło");
    }

    Employee employee = employeeRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new ApplicationException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

    final String token = jwtUtil.generateToken(request.getEmail(), employee.getRole().name());

    return new LoginResponse(token);
  }

  public void changePassword(ChangePasswordRequest request) {
    Employee employee = employeeRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new ApplicationException(HttpStatus.BAD_REQUEST, "Invalid email or password"));

    if (!passwordEncoder.matches(request.getCurrentPassword(), employee.getPassword())) {
      throw new ApplicationException(HttpStatus.BAD_REQUEST, "Nieprawidłowy email lub hasło");
    }

    employee.setPassword(passwordEncoder.encode(request.getNewPassword()));
    employeeRepository.save(employee);
  }
}
