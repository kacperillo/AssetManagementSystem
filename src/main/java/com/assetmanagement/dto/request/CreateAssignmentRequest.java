package com.assetmanagement.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAssignmentRequest {

  @NotNull(message = "Employee ID is required")
  private Long employeeId;

  @NotNull(message = "Asset ID is required")
  private Long assetId;

  @NotNull(message = "Assigned from date is required")
  private LocalDate assignedFrom;
}
