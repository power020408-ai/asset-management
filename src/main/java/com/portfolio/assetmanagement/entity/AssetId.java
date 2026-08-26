package com.portfolio.assetmanagement.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;


public class AssetId implements Serializable {

    private Long fund;
    private LocalDate navDate;
    private String assetId;

    public AssetId() {}

    public AssetId(Long fund, LocalDate navDate, String assetId) {
        this.fund = fund;
        this.navDate = navDate;
        this.assetId = assetId;
    }

    public Long getFund() {
        return fund;
    }

    public LocalDate getNavDate() {
        return navDate;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AssetId)) return false;
        AssetId that = (AssetId) o;
        return Objects.equals(fund, that.fund)
                && Objects.equals(navDate, that.navDate)
                && Objects.equals(assetId, that.assetId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fund, navDate, assetId);
    }
}
