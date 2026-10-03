package com.portfolio.assetmanagement.repository;

import com.portfolio.assetmanagement.entity.AssetMaster;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AssetMasterRepository extends JpaRepository<AssetMaster, UUID> {

    Optional<AssetMaster> findByAssetId(String assetId);
    Page<AssetMaster> findByAssetIdContainingIgnoreCase(String keyword, Pageable pageable);
}