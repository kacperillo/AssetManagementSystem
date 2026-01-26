package com.assetmanagement.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "employees")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Employee {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "employee_seq")
  @SequenceGenerator(name = "employee_seq", sequenceName = "employee_sequence", initialValue = 1000, allocationSize = 1)
  private Long id;

  @Column(nullable = false)
  private String fullName;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false)
  private String password;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Role role;

  @Column(name = "hired_from", nullable = false)
  private LocalDate hiredFrom;

  @Column(name = "hired_until")
  private LocalDate hiredUntil;

  @OneToMany(
          mappedBy = "employee",
          cascade = CascadeType.ALL,
          orphanRemoval = true
  )
  private List<Assignment> assignments = new ArrayList<>();
}
