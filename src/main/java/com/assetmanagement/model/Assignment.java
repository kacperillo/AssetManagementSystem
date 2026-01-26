package com.assetmanagement.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(
        name = "assignments",
        indexes = {
                @Index(name = "idx_assignment_asset", columnList = "asset_id"),
                @Index(name = "idx_assignment_employee", columnList = "employee_id"),
                @Index(name = "idx_assignment_assigned_from", columnList = "assigned_from"),
                @Index(name = "idx_assignment_assigned_until", columnList = "assigned_until")
        }
)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Assignment {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "assignment_seq")
  @SequenceGenerator(name = "assignment_seq", sequenceName = "assignment_sequence", initialValue = 1000, allocationSize = 1)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "asset_id", nullable = false)
  private Asset asset;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "employee_id", nullable = false)
  private Employee employee;

  @Column(name = "assigned_from", nullable = false)
  private LocalDate assignedFrom;

  @Column(name = "assigned_until")
  private LocalDate assignedUntil;

  public boolean isActive(LocalDate date) {
    return !date.isBefore(assignedFrom) &&
            (assignedUntil == null || !date.isAfter(assignedUntil));
  }
}
