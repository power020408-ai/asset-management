package com.portfolio.assetmanagement.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "asset_master")
public class AssetMaster {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "UUID", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "asset_id", unique = true)
    private String assetId;

    @Column(name = "asset_name")
    private String assetName;

    @Column(name = "asset_type")
    private String assetType;

    public String getAssetID() {return assetId;}
    public String getAssetName() {return assetName;}
    public String getAssetType() {return assetType;}
    public void setAssetID(String assetId) {
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
