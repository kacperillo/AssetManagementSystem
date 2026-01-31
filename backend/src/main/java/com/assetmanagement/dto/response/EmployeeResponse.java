package com.assetmanagement.dto.response;

import com.assetmanagement.model.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeResponse {

  private Long id;
  private String fullName;
  private String email;
  private Role role;
  private LocalDate hiredFrom;
  private LocalDate hiredUntil;
}
