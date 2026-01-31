package com.assetmanagement.dto.response;

import com.assetmanagement.model.AssetType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeAssetResponse {

  private AssetType assetType;
  private String vendor;
  private String model;
  private String seriesNumber;
  private LocalDate assignedFrom;
}
