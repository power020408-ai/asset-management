package com.portfolio.assetmanagement.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;

@Entity
@Table(name = "funds")
public class Fund {

    @Id
    @Column(name = "fund_id")
    private Long fundId;

    private String name;

    @Column(name = "unit_price")
    private BigDecimal unitPrice;

    @Column(name = "fund_shares")
    private BigDecimal fundShares;

    private BigDecimal nav;

    private LocalDate navDate;

    // Getter / Setter
    public Long getFundId() { return fundId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }


    public String getFundSharesStr() {
        DecimalFormat df = new DecimalFormat("#,###");
        return df.format(fundShares); }
    public void setFundShares(BigDecimal fundShares) { this.fundShares = fundShares; }

    public BigDecimal getNav() { return nav; }
    public void setNav(BigDecimal nav) { this.nav = nav; }

    public LocalDate getNavDate() { return navDate; }
    public void setNavDate(LocalDate navDate) { this.navDate = navDate; }
}
