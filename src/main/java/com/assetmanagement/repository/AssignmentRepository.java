package com.assetmanagement.repository;

import com.assetmanagement.model.Assignment;
import com.assetmanagement.model.Asset;
import com.assetmanagement.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

  List<Assignment> findByEmployee(Employee employee);

  List<Assignment> findByAsset(Asset asset);

  @Query("SELECT a FROM Assignment a WHERE a.employee.id = :employeeId AND a.assignedUntil IS NULL")
  List<Assignment> findActiveByEmployeeId(@Param("employeeId") Long employeeId);

  @Query("SELECT a FROM Assignment a WHERE a.asset.id = :assetId AND a.assignedUntil IS NULL")
  Optional<Assignment> findActiveByAssetId(@Param("assetId") Long assetId);
}
