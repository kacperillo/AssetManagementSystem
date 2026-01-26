package com.assetmanagement.repository;

import com.assetmanagement.model.Asset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AssetRepository extends JpaRepository<Asset, Long> {

  Optional<Asset> findBySeriesNumber(String seriesNumber);

  boolean existsBySeriesNumber(String seriesNumber);
}
