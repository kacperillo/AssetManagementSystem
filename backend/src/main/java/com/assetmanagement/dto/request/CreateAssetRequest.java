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

  @NotNull(message = "Typ zasobu jest wymagany")
  private AssetType assetType;

  @NotBlank(message = "Producent jest wymagany")
  private String vendor;

  @NotBlank(message = "Model jest wymagany")
  private String model;

  @NotBlank(message = "Numer seryjny jest wymagany")
  private String seriesNumber;
}
