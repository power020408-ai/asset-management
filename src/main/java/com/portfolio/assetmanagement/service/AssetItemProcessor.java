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

    private static final int MAX_INTEGER_DIGITS = 13;
    private static final int MAX_DECIMAL_PLACES = 2;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/M/d");
    @Override
    public AssetTable process(final AssetCsv asset) {
        final Long fundId = Long.parseLong(asset.fundIdStr());
        final String assetIdStr = asset.assetIdStr();
        final LocalDate navDate = LocalDate.parse(asset.navDateStr(),formatter);

        // BigDecimalへの変換と桁数検査をまとめて行う
        final BigDecimal amount = parseAndValidateAmount(asset.amountStr());
        
        final AssetTable transformedAsset = new AssetTable(fundId, assetIdStr, navDate, amount);

        log.info("Converting ({}) into ({})", asset, transformedAsset);

        return transformedAsset;
    }

    private BigDecimal parseAndValidateAmount(String amountStr) {
        if (amountStr == null || amountStr.isBlank()) {
            throw new IllegalArgumentException("[amount] is required");
        }

        final BigDecimal amount;
        try {
            amount = new BigDecimal(amountStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("[amount] format error", e);
        }

        // 末尾のゼロを除いた数値で桁数を判定する
        final BigDecimal normalizedAmount = amount.stripTrailingZeros();

        // precision - scale が整数部の桁数。
        // 0.01 のように結果が負になる場合は、整数部0桁として扱う。
        final int integerDigits = Math.max(
                0,
                normalizedAmount.precision() - normalizedAmount.scale()
        );

        if (integerDigits > MAX_INTEGER_DIGITS) {
            throw new IllegalArgumentException(
                    "[amount] integer part must be at most "
                            + MAX_INTEGER_DIGITS + " digits"
            );
        }

        if (normalizedAmount.scale() > MAX_DECIMAL_PLACES) {
            throw new IllegalArgumentException(
                    "[amount] must have at most "
                            + MAX_DECIMAL_PLACES + " decimal places"
            );
        }

        return amount;
    }
}
