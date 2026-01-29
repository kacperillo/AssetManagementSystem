package com.assetmanagement.service;

import com.assetmanagement.dto.request.CreateAssetRequest;
import com.assetmanagement.dto.response.AssetResponse;
import com.assetmanagement.dto.response.EmployeeAssetResponse;
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
import com.assetmanagement.dto.response.PagedResponse;

@Service
@RequiredArgsConstructor
public class AssetService {

  private final AssetRepository assetRepository;
  private final AssignmentRepository assignmentRepository;
  private final EmployeeRepository employeeRepository;

  @Transactional
  public AssetResponse createAsset(CreateAssetRequest request) {
    if (assetRepository.existsBySeriesNumber(request.getSeriesNumber())) {
      throw new ApplicationException(HttpStatus.BAD_REQUEST, "Numer seryjny jest już używany");
    }

    Asset asset = new Asset();
    asset.setAssetType(request.getAssetType());
    asset.setVendor(request.getVendor());
    asset.setModel(request.getModel());
    asset.setSeriesNumber(request.getSeriesNumber());
    asset.setActive(true);

    Asset saved = assetRepository.save(asset);
    return mapToAssetResponse(saved, null);
  }

  @Transactional(readOnly = true)
  public List<AssetResponse> getAllAssets() {
    return assetRepository.findAll().stream()
            .map(asset -> {
              Employee employee = assignmentRepository.findActiveByAssetId(asset.getId())
                      .map(Assignment::getEmployee)
                      .orElse(null);
              return mapToAssetResponse(asset, employee);
            })
            .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public PagedResponse<AssetResponse> getAllAssets(Pageable pageable) {
    Page<Asset> assetPage = assetRepository.findAll(pageable);

    List<AssetResponse> content = assetPage.getContent().stream()
            .map(asset -> {
              Employee employee = assignmentRepository.findActiveByAssetId(asset.getId())
                      .map(Assignment::getEmployee)
                      .orElse(null);
              return mapToAssetResponse(asset, employee);
            })
            .collect(Collectors.toList());

    return new PagedResponse<>(
            content,
            assetPage.getNumber(),
            assetPage.getSize(),
            assetPage.getTotalElements(),
            assetPage.getTotalPages(),
            assetPage.isLast()
    );
  }

  @Transactional(readOnly = true)
  public AssetResponse getAssetById(Long id) {
    Asset asset = assetRepository.findById(id)
            .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Zasób nie został znaleziony"));
    Employee employee = assignmentRepository.findActiveByAssetId(id)
            .map(Assignment::getEmployee)
            .orElse(null);
    return mapToAssetResponse(asset, employee);
  }

  @Transactional
  public void deactivateAsset(Long id) {
    Asset asset = assetRepository.findById(id)
            .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Zasób nie został znaleziony"));

    if (assignmentRepository.findActiveByAssetId(id).isPresent()) {
      throw new ApplicationException(HttpStatus.BAD_REQUEST,
              "Nie można dezaktywować zasobu, który jest przypisany do pracownika");
    }

    asset.setActive(false);
    assetRepository.save(asset);
  }

  @Transactional(readOnly = true)
  public List<EmployeeAssetResponse> getActiveAssetsByEmployeeEmail(String email) {
    Employee employee = employeeRepository.findByEmail(email)
            .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Pracownik nie został znaleziony"));

    return assignmentRepository.findActiveByEmployeeId(employee.getId()).stream()
            .map(this::mapToEmployeeAssetResponse)
            .collect(Collectors.toList());
  }

  private AssetResponse mapToAssetResponse(Asset asset, Employee employee) {
    AssetResponse response = new AssetResponse();
    response.setId(asset.getId());
    response.setAssetType(asset.getAssetType());
    response.setVendor(asset.getVendor());
    response.setModel(asset.getModel());
    response.setSeriesNumber(asset.getSeriesNumber());
    response.setActive(asset.isActive());

    if (employee != null) {
      response.setAssignedEmployeeId(employee.getId());
      response.setAssignedEmployeeFullName(employee.getFullName());
      response.setAssignedEmployeeEmail(employee.getEmail());
    }

    return response;
  }

  private EmployeeAssetResponse mapToEmployeeAssetResponse(Assignment assignment) {
    Asset asset = assignment.getAsset();
    EmployeeAssetResponse response = new EmployeeAssetResponse();
    response.setAssetType(asset.getAssetType());
    response.setVendor(asset.getVendor());
    response.setModel(asset.getModel());
    response.setSeriesNumber(asset.getSeriesNumber());
    response.setAssignedFrom(assignment.getAssignedFrom());

    return response;
  }

}
