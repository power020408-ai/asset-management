package com.portfolio.assetmanagement.service;

import com.portfolio.assetmanagement.entity.AssetId;
import com.portfolio.assetmanagement.entity.Asset;
import com.portfolio.assetmanagement.entity.Fund;
import com.portfolio.assetmanagement.repository.AssetRepository;
import com.portfolio.assetmanagement.repository.FundRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class AssetCsvService {
    private static final Logger log = LoggerFactory.getLogger(AssetCsvService.class);
    private final AssetRepository assetRepository;
    private final FundRepository fundRepository;

    public AssetCsvService(AssetRepository assetRepository, FundRepository fundRepository) {
        this.assetRepository = assetRepository;
        this.fundRepository = fundRepository;
    }

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/M/d");

    public void importCsv(MultipartFile file) {
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(file.getInputStream())
            );

            String line;
            while ((line = reader.readLine()) != null) {
                String[] cols = line.split(",");
                Long fundId = Long.parseLong(cols[0]);
                String assetIdStr = cols[1];            // ★ varchar(10)
                LocalDate navDate = LocalDate.parse(cols[2],formatter); // ★ 日付
                String assetName = cols[3];

                BigDecimal amount;
                try {
                    String valueStr = cols[4].trim();
                    amount = new BigDecimal(valueStr);
                } catch (NumberFormatException e) {
                    throw new RuntimeException("[amount] format error", e);
                }


                Fund fund = fundRepository.findById(fundId)
                        .orElseThrow(() -> new RuntimeException("Fund not found"));

                // ★ 複合主キーを生成
                AssetId id = new AssetId(fundId, navDate, assetIdStr);

                log.info("fundId = {}", fundId);
                log.info("assetIdStr: {}", assetIdStr);
                log.info("navDate = {}", navDate);
                log.info("assetName = {}", assetName);
                log.info("amount = {}", amount);

                Asset asset = assetRepository.findById(id)
                        .orElse(new Asset(assetIdStr, fund, navDate));
                asset.setName(assetName);
                asset.setAmount(amount);

                if (assetRepository.existsById(id)) {
                    log.info("Updating existing asset: {}", id);
                } else {
                    log.info("Inserting new asset: {}", id);
                }

                // ★ upsert（同じ fund_id + nav_date + asset_id なら更新）
                assetRepository.save(asset);

            }

        } catch (Exception e) {
            throw new RuntimeException("CSV import failed", e);
        }
    }
}
