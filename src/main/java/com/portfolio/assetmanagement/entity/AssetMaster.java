package com.portfolio.assetmanagement.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "asset_master")
public class AssetMaster {

    @Id @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    @Column(columnDefinition = "UUID", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "asset_id", unique = true)
    private String assetId;

    @Column(name = "asset_name")
    private String assetName;

    @Column(name = "asset_type")
    private String assetType;

    public String getAssetId() {return assetId;}
    public String getAssetName() {return assetName;}
    public String getAssetType() {return assetType;}
    public void setAssetId(String assetId) {
        this.assetId = assetId;
        return;
    }
    public void setAssetName(String assetName) {
        this.assetName = assetName;
        return;
    }
    public void setAssetType(String assetType) {
        this.assetType = assetType;
        return;
    }
}
