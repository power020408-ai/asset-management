package com.portfolio.assetmanagement.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "fund_nav_history")
@IdClass(FundHistoryId.class)
public class FundNavHistory {

    @Id
    @ManyToOne
    @JoinColumn(name = "fund_id")
    private Fund fund;

    @Id
    @Column(name = "nav_date")
    private LocalDate navDate;

    @Column(name = "nav")
    private BigDecimal nav;

    @Column(name = "unit_price")
    private BigDecimal unitPrice;

    @Column(name = "fund_shares")
    private BigDecimal fundShares;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public FundNavHistory() {}

    public FundNavHistory(Fund fund, LocalDate navDate) {
        this.fund = fund;
        this.navDate = navDate;
    }

    public LocalDate getNavDate() {
        return navDate;
    }
    public void setNavDate(LocalDate navDate) {
        this.navDate = navDate;
    }

    public Fund getFund() {
        return fund;
    }

    public void setFund(Fund fund) {
        this.fund = fund;
    }

    public BigDecimal getNav() {
        return nav;
    }

    public void setNav(BigDecimal nav) {
        this.nav = nav;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getFundShares() {
        return fundShares;
    }

    public void setFundShares(BigDecimal fundShares) {
        this.fundShares = fundShares;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "FundNavHistory{" +
                "fund=" + fund +
                ", navDate=" + navDate +
                ", nav=" + nav +
                ", unitPrice=" + unitPrice +
                ", fundShares=" + fundShares +
                ", createdAt=" + createdAt +
                '}';
    }

}
