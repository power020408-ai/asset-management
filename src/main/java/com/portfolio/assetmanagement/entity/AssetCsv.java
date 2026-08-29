package com.portfolio.assetmanagement.entity;

public record AssetCsv(
        String fundIdStr, String assetIdStr, String navDateStr,
        String assetName, String amountStr) {}
