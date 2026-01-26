package com.assetmanagement.service;

import com.assetmanagement.dto.request.CreateEmployeeRequest;
import com.assetmanagement.dto.response.EmployeeResponse;
import com.assetmanagement.exception.ApplicationException;
import com.assetmanagement.model.Employee;
import com.assetmanagement.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeService {

  private final EmployeeRepository employeeRepository;
  private final PasswordEncoder passwordEncoder;

  @Transactional
  public EmployeeResponse createEmployee(CreateEmployeeRequest request) {
    if (employeeRepository.existsByEmail(request.getEmail())) {
      throw new ApplicationException(HttpStatus.BAD_REQUEST, "Email is already in use");
    }

    Employee employee = new Employee();
    employee.setFullName(request.getFullName());
    employee.setEmail(request.getEmail());
    employee.setPassword(passwordEncoder.encode(request.getPassword()));
    employee.setRole(request.getRole());
    employee.setHiredFrom(request.getHiredFrom());
    employee.setHiredUntil(request.getHiredUntil());

    Employee saved = employeeRepository.save(employee);
    return mapToResponse(saved);
  }

  @Transactional(readOnly = true)
  public List<EmployeeResponse> getAllEmployees() {
    return employeeRepository.findAll().stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public EmployeeResponse getEmployeeById(Long id) {
    Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Employee not found"));
    return mapToResponse(employee);
  }

  private EmployeeResponse mapToResponse(Employee employee) {
    return new EmployeeResponse(
            employee.getId(),
            employee.getFullName(),
            employee.getEmail(),
            employee.getRole(),
            employee.getHiredFrom(),
            employee.getHiredUntil()
    );
  }
}
