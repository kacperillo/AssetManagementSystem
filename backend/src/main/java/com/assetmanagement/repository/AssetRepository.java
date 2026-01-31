package com.assetmanagement.repository;

import com.assetmanagement.model.Asset;
import com.assetmanagement.model.AssetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AssetRepository extends JpaRepository<Asset, Long> {

  Optional<Asset> findBySeriesNumber(String seriesNumber);

  boolean existsBySeriesNumber(String seriesNumber);

  Page<Asset> findByIsActive(boolean isActive, Pageable pageable);

  Page<Asset> findByAssetType(AssetType assetType, Pageable pageable);

  Page<Asset> findByIsActiveAndAssetType(boolean isActive, AssetType assetType, Pageable pageable);

  @Query("SELECT a FROM Asset a WHERE EXISTS (SELECT 1 FROM Assignment asg WHERE asg.asset = a AND asg.assignedUntil IS NULL)")
  Page<Asset> findAssigned(Pageable pageable);

  @Query("SELECT a FROM Asset a WHERE NOT EXISTS (SELECT 1 FROM Assignment asg WHERE asg.asset = a AND asg.assignedUntil IS NULL)")
  Page<Asset> findUnassigned(Pageable pageable);

  @Query("SELECT a FROM Asset a WHERE a.isActive = :isActive AND EXISTS (SELECT 1 FROM Assignment asg WHERE asg.asset = a AND asg.assignedUntil IS NULL)")
  Page<Asset> findByIsActiveAndAssigned(@Param("isActive") boolean isActive, Pageable pageable);

  @Query("SELECT a FROM Asset a WHERE a.isActive = :isActive AND NOT EXISTS (SELECT 1 FROM Assignment asg WHERE asg.asset = a AND asg.assignedUntil IS NULL)")
  Page<Asset> findByIsActiveAndUnassigned(@Param("isActive") boolean isActive, Pageable pageable);

  @Query("SELECT a FROM Asset a WHERE a.assetType = :assetType AND EXISTS (SELECT 1 FROM Assignment asg WHERE asg.asset = a AND asg.assignedUntil IS NULL)")
  Page<Asset> findByAssetTypeAndAssigned(@Param("assetType") AssetType assetType, Pageable pageable);

  @Query("SELECT a FROM Asset a WHERE a.assetType = :assetType AND NOT EXISTS (SELECT 1 FROM Assignment asg WHERE asg.asset = a AND asg.assignedUntil IS NULL)")
  Page<Asset> findByAssetTypeAndUnassigned(@Param("assetType") AssetType assetType, Pageable pageable);

  @Query("SELECT a FROM Asset a WHERE a.isActive = :isActive AND a.assetType = :assetType AND EXISTS (SELECT 1 FROM Assignment asg WHERE asg.asset = a AND asg.assignedUntil IS NULL)")
  Page<Asset> findByIsActiveAndAssetTypeAndAssigned(@Param("isActive") boolean isActive, @Param("assetType") AssetType assetType, Pageable pageable);

  @Query("SELECT a FROM Asset a WHERE a.isActive = :isActive AND a.assetType = :assetType AND NOT EXISTS (SELECT 1 FROM Assignment asg WHERE asg.asset = a AND asg.assignedUntil IS NULL)")
  Page<Asset> findByIsActiveAndAssetTypeAndUnassigned(@Param("isActive") boolean isActive, @Param("assetType") AssetType assetType, Pageable pageable);
}
