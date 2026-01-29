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

  @NotNull(message = "Pracownik jest wymagany")
  private Long employeeId;

  @NotNull(message = "Zasób jest wymagany")
  private Long assetId;

  @NotNull(message = "Data rozpoczęcia jest wymagana")
  private LocalDate assignedFrom;
}
