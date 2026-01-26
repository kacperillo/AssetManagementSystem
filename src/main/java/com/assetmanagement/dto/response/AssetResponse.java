package com.assetmanagement.dto.response;

import com.assetmanagement.model.AssetType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssetResponse {

  private Long id;
  private AssetType assetType;
  private String vendor;
  private String model;
  private String seriesNumber;
  private boolean isActive;
  private Long assignedEmployeeId;
  private String assignedEmployeeFullName;
  private String assignedEmployeeEmail;
}
