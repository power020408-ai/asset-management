package com.portfolio.assetmanagement.service;

import com.portfolio.assetmanagement.entity.*;
import com.portfolio.assetmanagement.repository.AssetRepository;
import com.portfolio.assetmanagement.repository.FundNavHistoryRepository;
import com.portfolio.assetmanagement.repository.FundRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class FundService {
    private static final Logger log = LoggerFactory.getLogger(FundService.class);
    private final FundRepository fundRepository;
    private final AssetRepository assetRepository;
    private final FundNavHistoryRepository fundNavHistoryRepository;


    public FundService(FundRepository fundRepository,
                       AssetRepository assetRepository,
                       FundNavHistoryRepository fundNavHistoryRepository) {
        this.fundRepository = fundRepository;
        this.assetRepository = assetRepository;
        this.fundNavHistoryRepository = fundNavHistoryRepository;
    }

    public Fund calculateNav(Long fundId, LocalDate navDate) {

        Fund fund = fundRepository.findById(fundId)
                .orElseThrow(() -> new RuntimeException("Fund not found"));

        // ★ 今日の資産だけ取得（複合主キーの fundId + navDate）
        List<Asset> assets = assetRepository.findByFundAndNavDate(fund, navDate);
        BigDecimal nav = assets.stream()
                .filter(asset -> asset.getAssetId() != null && !"FUND_SHARES".equals(asset.getAssetId()))
                .map(Asset::getAmount)
                .filter(Objects::nonNull) // null の金額がある場合の NullPointerException 防止
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal fundShares = assets.stream()
                .filter(asset -> asset.getAssetId()!= null && "FUND_SHARES".equals(asset.getAssetId()))
                .map(Asset::getAmount)
                .filter(Objects::nonNull) // null の金額がある場合の NullPointerException 防止
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // ★ NAV を計算（10,000 * 総資産 ÷ 口数）
        BigDecimal unitPrice = BigDecimal.ZERO;
        if (fundShares.compareTo(BigDecimal.ZERO) != 0) {
            unitPrice = BigDecimal.valueOf(10000)
                    .multiply(nav)
                    .divide(fundShares, 0, RoundingMode.DOWN);
                    //                  ^桁数    ^端数処理（四捨五入など）
        }

        // ★ 複合主キーを生成
        FundHistoryId id = new FundHistoryId(fundId, navDate);

        // ★ NAV 履歴を upsert（同じ fundId + navDate があれば更新）
        FundNavHistory history = fundNavHistoryRepository
                .findById(id)
                .orElse(new FundNavHistory(fund, navDate));

        history.setNav(nav);
        history.setUnitPrice(unitPrice);
        history.setFundShares(fundShares);


        log.info("fund = {}", fund);
        log.info("fundId = {}", fundId);
        log.info("navDate = {}", navDate);
        log.info("nav = {}", nav);
        log.info("fundShares = {}", fundShares);
        log.info("unitPrice = {}", unitPrice);

        fundNavHistoryRepository.save(history);

        // ★ Fund の最新 NAV を更新
        fund.setNav(nav);
        fund.setNavDate(navDate);
        fund.setUnitPrice(unitPrice);
        fund.setFundShares(fundShares);
        fundRepository.save(fund);

        return fund;
    }
}
