package com.portfolio.assetmanagement.service;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.portfolio.assetmanagement.entity.AssetCsv;
import com.portfolio.assetmanagement.entity.AssetTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.batch.infrastructure.item.ItemProcessor;

public class AssetItemProcessor implements ItemProcessor<AssetCsv, AssetTable> {

    private static final Logger log = LoggerFactory.getLogger(AssetItemProcessor.class);
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/M/d");
    @Override
    public AssetTable process(final AssetCsv asset) {
        final Long fundId = Long.parseLong(asset.fundIdStr());
        final String assetIdStr = asset.assetIdStr();
        final LocalDate navDate = LocalDate.parse(asset.navDateStr(),formatter);
        final String assetName = asset.assetName();
        final String amountStr = asset.amountStr();
        final BigDecimal amount;
        try {
            amount = new BigDecimal(amountStr);
        } catch (NumberFormatException e) {
            throw new RuntimeException("[amount] format error", e);
        }

        final AssetTable transformedAsset = new AssetTable(fundId, assetIdStr, navDate, assetName, amount);

        log.info("Converting ({}) into ({})", asset, transformedAsset);

        return transformedAsset;
    }

}
