package com.portfolio.assetmanagement.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;


public class FundHistoryId implements Serializable {

    private Long fund;
    private LocalDate navDate;

    public FundHistoryId() {}

    public FundHistoryId(Long fund, LocalDate navDate) {  //, String assetId) {
        this.fund = fund;
        this.navDate = navDate;
    }

    public Long getFundId() {
        return fund;
    }

    public LocalDate getNavDate() {
        return navDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FundHistoryId)) return false;
        FundHistoryId that = (FundHistoryId) o;
        return Objects.equals(fund, that.getFundId())
                && Objects.equals(navDate, that.getNavDate()) ;
                //&& Objects.equals(assetId, that.assetId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fund, navDate ); // , assetId);
    }
}
