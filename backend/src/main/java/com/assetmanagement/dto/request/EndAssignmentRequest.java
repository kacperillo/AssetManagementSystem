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
public class EndAssignmentRequest {

  @NotNull(message = "Data zakończenia jest wymagana")
  @PastOrPresent(message = "Data nie może być w przyszłości")
  private LocalDate assignedUntil;
}
