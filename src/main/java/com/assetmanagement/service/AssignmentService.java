package com.assetmanagement.service;

import com.assetmanagement.dto.request.CreateAssignmentRequest;
import com.assetmanagement.dto.request.EndAssignmentRequest;
import com.assetmanagement.dto.response.AssignmentResponse;
import com.assetmanagement.dto.response.PagedResponse;
import com.assetmanagement.exception.ApplicationException;
import com.assetmanagement.model.Asset;
import com.assetmanagement.model.Assignment;
import com.assetmanagement.model.Employee;
import com.assetmanagement.repository.AssetRepository;
import com.assetmanagement.repository.AssignmentRepository;
import com.assetmanagement.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssignmentService {

  private final AssignmentRepository assignmentRepository;
  private final EmployeeRepository employeeRepository;
  private final AssetRepository assetRepository;

  @Transactional
  public AssignmentResponse createAssignment(CreateAssignmentRequest request) {
    Employee employee = employeeRepository.findById(request.getEmployeeId())
            .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Employee not found"));

    Asset asset = assetRepository.findById(request.getAssetId())
            .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Asset not found"));

    if (!asset.isActive()) {
      throw new ApplicationException(HttpStatus.BAD_REQUEST, "Cannot assign inactive asset");
    }

    if (assignmentRepository.findActiveByAssetId(asset.getId()).isPresent()) {
      throw new ApplicationException(HttpStatus.BAD_REQUEST,
              "Asset is already assigned to another employee");
    }

    Assignment assignment = new Assignment();
    assignment.setEmployee(employee);
    assignment.setAsset(asset);
    assignment.setAssignedFrom(request.getAssignedFrom());

    Assignment saved = assignmentRepository.save(assignment);
    return mapToResponse(saved);
  }

  @Transactional
  public AssignmentResponse endAssignment(Long assignmentId, EndAssignmentRequest request) {
    Assignment assignment = assignmentRepository.findById(assignmentId)
            .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Assignment not found"));

    if (assignment.getAssignedUntil() != null) {
      throw new ApplicationException(HttpStatus.BAD_REQUEST, "Assignment is already ended");
    }

    assignment.setAssignedUntil(request.getAssignedUntil());
    Assignment saved = assignmentRepository.save(assignment);
    return mapToResponse(saved);
  }

  @Transactional(readOnly = true)
  public List<AssignmentResponse> getAllAssignments() {
    return assignmentRepository.findAll().stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public PagedResponse<AssignmentResponse> getAllAssignments(Pageable pageable) {
    Page<Assignment> assignmentPage = assignmentRepository.findAll(pageable);

    List<AssignmentResponse> content = assignmentPage.getContent().stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());

    return new PagedResponse<>(
            content,
            assignmentPage.getNumber(),
            assignmentPage.getSize(),
            assignmentPage.getTotalElements(),
            assignmentPage.getTotalPages(),
            assignmentPage.isLast()
    );
  }

  @Transactional(readOnly = true)
  public List<AssignmentResponse> getAssignmentsByEmployeeId(Long employeeId) {
    Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Employee not found"));

    return assignmentRepository.findByEmployee(employee).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<AssignmentResponse> getAssignmentsByAssetId(Long assetId) {
    Asset asset = assetRepository.findById(assetId)
            .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Asset not found"));

    return assignmentRepository.findByAsset(asset).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<AssignmentResponse> getAssignmentHistoryByEmployeeEmail(String email) {
    Employee employee = employeeRepository.findByEmail(email)
            .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Employee not found"));

    return assignmentRepository.findByEmployee(employee).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
  }

  private AssignmentResponse mapToResponse(Assignment assignment) {
    return new AssignmentResponse(
            assignment.getId(),
            assignment.getAsset().getId(),
            assignment.getAsset().getAssetType().name(),
            assignment.getAsset().getVendor(),
            assignment.getAsset().getModel(),
            assignment.getAsset().getSeriesNumber(),
            assignment.getEmployee().getId(),
            assignment.getEmployee().getFullName(),
            assignment.getAssignedFrom(),
            assignment.getAssignedUntil(),
            assignment.getAssignedUntil() == null
    );
  }
}
