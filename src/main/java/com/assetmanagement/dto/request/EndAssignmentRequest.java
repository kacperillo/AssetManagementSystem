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

  @NotNull(message = "Data zakończenia jest wymagana")
  private LocalDate assignedUntil;
}
