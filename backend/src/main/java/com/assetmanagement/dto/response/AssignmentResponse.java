package com.assetmanagement.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentResponse {

  private Long id;
  private Long assetId;
  private String assetType;
  private String vendor;
  private String model;
  private String seriesNumber;
  private Long employeeId;
  private String employeeFullName;
  private LocalDate assignedFrom;
  private LocalDate assignedUntil;
  @JsonProperty("isActive")
  private boolean isActive;
}
