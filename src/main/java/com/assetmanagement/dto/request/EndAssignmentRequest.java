package com.assetmanagement.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EndAssignmentRequest {

  @NotNull(message = "Assigned until date is required")
  private LocalDate assignedUntil;
}
