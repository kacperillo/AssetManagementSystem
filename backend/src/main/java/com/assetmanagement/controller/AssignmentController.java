package com.assetmanagement.controller;

import com.assetmanagement.dto.request.CreateAssignmentRequest;
import com.assetmanagement.dto.request.EndAssignmentRequest;
import com.assetmanagement.dto.response.AssignmentResponse;
import com.assetmanagement.dto.response.PagedResponse;
import com.assetmanagement.service.AssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AssignmentController {

  private final AssignmentService assignmentService;

  @PostMapping("/admin/assignments")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<AssignmentResponse> createAssignment(@Valid @RequestBody CreateAssignmentRequest request) {
    AssignmentResponse response = assignmentService.createAssignment(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PutMapping("/admin/assignments/{id}/end")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<AssignmentResponse> endAssignment(
          @PathVariable Long id,
          @Valid @RequestBody EndAssignmentRequest request) {
    AssignmentResponse response = assignmentService.endAssignment(id, request);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/admin/assignments")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<PagedResponse<AssignmentResponse>> getAllAssignments(
          @RequestParam(required = false) Long employeeId,
          @RequestParam(required = false) Long assetId,
          @RequestParam(required = false) Boolean isActive,
          @RequestParam(defaultValue = "0") int page,
          @RequestParam(defaultValue = "20") int size,
          @RequestParam(defaultValue = "id") String sortBy,
          @RequestParam(defaultValue = "asc") String sortDir) {

    Sort sort = sortDir.equalsIgnoreCase("desc")
            ? Sort.by(sortBy).descending()
            : Sort.by(sortBy).ascending();
    Pageable pageable = PageRequest.of(page, size, sort);
    PagedResponse<AssignmentResponse> assignments = assignmentService.getAllAssignments(pageable, isActive, employeeId, assetId);
    return ResponseEntity.ok(assignments);
  }

  @GetMapping("/employee/assignments")
  @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
  public ResponseEntity<List<AssignmentResponse>> getMyAssignmentHistory(Authentication authentication) {
    String email = authentication.getName();
    List<AssignmentResponse> assignments = assignmentService.getAssignmentHistoryByEmployeeEmail(email);
    return ResponseEntity.ok(assignments);
  }
}
