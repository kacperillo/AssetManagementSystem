package com.assetmanagement.controller;

import com.assetmanagement.dto.request.CreateEmployeeRequest;
import com.assetmanagement.dto.response.EmployeeResponse;
import com.assetmanagement.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/employees")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class EmployeeController {

  private final EmployeeService employeeService;

  @PostMapping
  public ResponseEntity<EmployeeResponse> createEmployee(@Valid @RequestBody CreateEmployeeRequest request) {
    EmployeeResponse response = employeeService.createEmployee(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping
  public ResponseEntity<List<EmployeeResponse>> getAllEmployees() {
    List<EmployeeResponse> employees = employeeService.getAllEmployees();
    return ResponseEntity.ok(employees);
  }

  @GetMapping("/{id}")
  public ResponseEntity<EmployeeResponse> getEmployeeById(@PathVariable Long id) {
    EmployeeResponse employee = employeeService.getEmployeeById(id);
    return ResponseEntity.ok(employee);
  }
}
