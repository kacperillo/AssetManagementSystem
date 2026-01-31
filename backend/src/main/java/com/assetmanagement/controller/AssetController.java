package com.assetmanagement.controller;

import com.assetmanagement.dto.request.CreateAssetRequest;
import com.assetmanagement.dto.response.AssetResponse;
import com.assetmanagement.dto.response.EmployeeAssetResponse;
import com.assetmanagement.dto.response.PagedResponse;
import com.assetmanagement.model.AssetType;
import com.assetmanagement.service.AssetService;
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
public class AssetController {

  private final AssetService assetService;

  @PostMapping("/admin/assets")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<AssetResponse> createAsset(@Valid @RequestBody CreateAssetRequest request) {
    AssetResponse response = assetService.createAsset(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("/admin/assets")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<PagedResponse<AssetResponse>> getAllAssets(
          @RequestParam(defaultValue = "0") int page,
          @RequestParam(defaultValue = "20") int size,
          @RequestParam(defaultValue = "id") String sortBy,
          @RequestParam(defaultValue = "asc") String sortDir,
          @RequestParam(required = false) Boolean isActive,
          @RequestParam(required = false) AssetType assetType,
          @RequestParam(required = false) Boolean isAssigned) {
    Sort sort = sortDir.equalsIgnoreCase("desc")
            ? Sort.by(sortBy).descending()
            : Sort.by(sortBy).ascending();
    Pageable pageable = PageRequest.of(page, size, sort);
    PagedResponse<AssetResponse> assets = assetService.getAllAssets(pageable, isActive, assetType, isAssigned);
    return ResponseEntity.ok(assets);
  }

  @GetMapping("/admin/assets/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<AssetResponse> getAssetById(@PathVariable Long id) {
    AssetResponse asset = assetService.getAssetById(id);
    return ResponseEntity.ok(asset);
  }

  @PutMapping("/admin/assets/{id}/deactivate")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> deactivateAsset(@PathVariable Long id) {
    assetService.deactivateAsset(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/employee/assets")
  @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
  public ResponseEntity<List<EmployeeAssetResponse>> getMyActiveAssets(Authentication authentication) {
    String email = authentication.getName();
    List<EmployeeAssetResponse> assets = assetService.getActiveAssetsByEmployeeEmail(email);
    return ResponseEntity.ok(assets);
  }
}
