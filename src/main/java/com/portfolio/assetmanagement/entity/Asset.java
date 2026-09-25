package com.portfolio.assetmanagement.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Entity
@Table(name = "assets")
@IdClass(AssetId.class) // ★ 複合主キーの AssetId を紐づける

public class Asset {

    @Id
    @ManyToOne
    @JoinColumn(name = "fund_id")
    private Fund fund;

    @Id
    @Column(name = "asset_id")
    private String assetId;

    @Id
    @Column(name = "nav_date")
    private LocalDate navDate;

    @ManyToOne(optional = true)
    @JoinColumn(insertable = false,
            updatable = false,
            name = "asset_id",
            referencedColumnName = "asset_id")

    private AssetMaster assetMaster;

    @Column(name = "amount")
    private BigDecimal amount;

    public Asset() {}

    public Asset (String assetId, Fund fund, LocalDate navDate) {
        this.assetId = assetId;
        this.fund = fund;
        this.navDate = navDate;
    }

    public String getAssetId() {
       return assetId;
    }

    public Fund getFund() {
        return fund;
    }
    public void setFund(Fund fund) {
        this.fund = fund;
    }

    public AssetMaster getAssetMaster() {
       return assetMaster;
    }

    public String getAssetType() {
        return Optional.ofNullable(getAssetMaster())
                .map(AssetMaster::getAssetType)
                .orElse("");
    }

    public String getAssetName() {
        return Optional.ofNullable(getAssetMaster())
                .map(AssetMaster::getAssetName)
                .orElse("");
    }

    public void setNavDate(LocalDate navDate) { this.navDate = navDate;}

    public BigDecimal getAmount() {
        return amount;
    }
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
