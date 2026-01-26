package com.assetmanagement.dto.request;

import com.assetmanagement.model.AssetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAssetRequest {

  @NotNull(message = "Asset type is required")
  private AssetType assetType;

  @NotBlank(message = "Vendor is required")
  private String vendor;

  @NotBlank(message = "Model is required")
  private String model;

  @NotBlank(message = "Series number is required")
  private String seriesNumber;
}
