package com.portfolio.assetmanagement.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AssetTable(Long fundId, String assetIdStr,
                         LocalDate navDate, BigDecimal amount) {}
