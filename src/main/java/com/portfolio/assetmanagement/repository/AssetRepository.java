package com.portfolio.assetmanagement.repository;

import com.portfolio.assetmanagement.entity.Asset;
import com.portfolio.assetmanagement.entity.AssetId;
import com.portfolio.assetmanagement.entity.Fund;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.time.LocalDate;
//public interface AssetRepository extends JpaRepository<Asset, Long> {
//}

public interface AssetRepository extends JpaRepository<Asset, AssetId> {

    List<Asset> findByFundAndNavDate(Fund fund, LocalDate navDate);
}
