package com.assetmanagement.repository;

import com.assetmanagement.model.Assignment;
import com.assetmanagement.model.Asset;
import com.assetmanagement.model.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

  @Query("SELECT a FROM Assignment a WHERE a.assignedUntil IS NULL")
  Page<Assignment> findActive(Pageable pageable);

  @Query("SELECT a FROM Assignment a WHERE a.assignedUntil IS NOT NULL")
  Page<Assignment> findEnded(Pageable pageable);

  // Filtrowanie po pracowniku z paginacją
  @Query("SELECT a FROM Assignment a WHERE a.employee.id = :employeeId")
  Page<Assignment> findByEmployeeId(@Param("employeeId") Long employeeId, Pageable pageable);

  @Query("SELECT a FROM Assignment a WHERE a.employee.id = :employeeId AND a.assignedUntil IS NULL")
  Page<Assignment> findActiveByEmployeeIdPaged(@Param("employeeId") Long employeeId, Pageable pageable);

  @Query("SELECT a FROM Assignment a WHERE a.employee.id = :employeeId AND a.assignedUntil IS NOT NULL")
  Page<Assignment> findEndedByEmployeeId(@Param("employeeId") Long employeeId, Pageable pageable);

  // Filtrowanie po zasobie z paginacją
  @Query("SELECT a FROM Assignment a WHERE a.asset.id = :assetId")
  Page<Assignment> findByAssetId(@Param("assetId") Long assetId, Pageable pageable);

  @Query("SELECT a FROM Assignment a WHERE a.asset.id = :assetId AND a.assignedUntil IS NULL")
  Page<Assignment> findActiveByAssetIdPaged(@Param("assetId") Long assetId, Pageable pageable);

  @Query("SELECT a FROM Assignment a WHERE a.asset.id = :assetId AND a.assignedUntil IS NOT NULL")
  Page<Assignment> findEndedByAssetId(@Param("assetId") Long assetId, Pageable pageable);

  // Filtrowanie po pracowniku i zasobie z paginacją
  @Query("SELECT a FROM Assignment a WHERE a.employee.id = :employeeId AND a.asset.id = :assetId")
  Page<Assignment> findByEmployeeIdAndAssetId(@Param("employeeId") Long employeeId, @Param("assetId") Long assetId, Pageable pageable);

  @Query("SELECT a FROM Assignment a WHERE a.employee.id = :employeeId AND a.asset.id = :assetId AND a.assignedUntil IS NULL")
  Page<Assignment> findActiveByEmployeeIdAndAssetId(@Param("employeeId") Long employeeId, @Param("assetId") Long assetId, Pageable pageable);

  @Query("SELECT a FROM Assignment a WHERE a.employee.id = :employeeId AND a.asset.id = :assetId AND a.assignedUntil IS NOT NULL")
  Page<Assignment> findEndedByEmployeeIdAndAssetId(@Param("employeeId") Long employeeId, @Param("assetId") Long assetId, Pageable pageable);
}
