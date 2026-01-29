package com.assetmanagement.dto.request;

import com.assetmanagement.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateEmployeeRequest {

  @NotBlank(message = "Imię i nazwisko jest wymagane")
  private String fullName;

  @NotBlank(message = "Email jest wymagany")
  @Email(message = "Nieprawidłowy format email")
  private String email;

  @NotBlank(message = "Hasło jest wymagane")
  private String password;

  @NotNull(message = "Rola jest wymagana")
  private Role role;

  @NotNull(message = "Data zatrudnienia jest wymagana")
  private LocalDate hiredFrom;

  private LocalDate hiredUntil;
}
