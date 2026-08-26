package com.portfolio.assetmanagement.repository;

import com.portfolio.assetmanagement.entity.Fund;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FundRepository extends JpaRepository<Fund, Long> {
}