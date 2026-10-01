package com.portfolio.assetmanagement.repository;

import com.portfolio.assetmanagement.entity.Fund;
import com.portfolio.assetmanagement.entity.FundNavHistory;
import com.portfolio.assetmanagement.entity.FundHistoryId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FundNavHistoryRepository extends JpaRepository<FundNavHistory, FundHistoryId> {
    //List<FundNavHistory> findByFundOrderByNavDateDesc(Fund fund);
    Optional<FundNavHistory> findByFundAndNavDate(Fund fund, LocalDate navDate);
    Page<FundNavHistory> findByFund(Fund fund, Pageable page);
}
