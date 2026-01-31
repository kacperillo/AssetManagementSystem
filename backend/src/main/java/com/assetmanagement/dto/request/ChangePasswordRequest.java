package com.assetmanagement.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequest {

  @NotBlank(message = "Email jest wymagany")
  @Email(message = "Nieprawidłowy format email")
  private String email;

  @NotBlank(message = "Aktualne hasło jest wymagane")
  private String currentPassword;

  @NotBlank(message = "Nowe hasło jest wymagane")
  private String newPassword;
}
