package com.assetmanagement.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
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
  @PastOrPresent(message = "Data nie może być w przyszłości")
  private LocalDate assignedFrom;
}
