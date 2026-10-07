package com.portfolio.assetmanagement.repository;


import com.portfolio.assetmanagement.entity.AssetMaster;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
//import org.springframework.boot.test.autoconfigure.jdbc.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("AssetMasterRepository DB層テスト")
class AssetMasterRepositoryTest {

    @Autowired
    private AssetMasterRepository assetMasterRepository;

    @Autowired
    private TestEntityManager entityManager; // テストデータを直接DBに流し込むための便利クラス

    @BeforeEach
    void setUp() {
        // テスト用の銘柄マスターをあらかじめDBに投入
        entityManager.persist(createAsset("A001", "全米株式インデックス", "株式"));
        entityManager.persist(createAsset("a002", "全世界株式インデックス", "株式")); // 小文字
        entityManager.persist(createAsset("B001", "国内債券オープン", "債券"));
        entityManager.flush();
    }

    private AssetMaster createAsset(String id, String name, String type) {
        AssetMaster a = new AssetMaster();
        a.setAssetId(id);
        a.setAssetName(name);
        a.setAssetType(type);
        return a;
    }

    @Test
    @DisplayName("findByAssetId: 銘柄コード完全一致で取得できる")
    void findByAssetId_完全一致() {
        Optional<AssetMaster> result = assetMasterRepository.findByAssetId("A001");

        assertThat(result).isPresent();
        assertThat(result.get().getAssetName()).isEqualTo("全米株式インデックス");
    }

    @Test
    @DisplayName("findByAssetIdContainingIgnoreCase: 大文字・小文字を区別せず部分一致で検索できる")
    void findByAssetIdContainingIgnoreCase_大小無視_部分一致() {
        // 小文字 "a" で検索しても "A001" と "a002" の両方がヒットすること
        Page<AssetMaster> page = assetMasterRepository.findByAssetIdContainingIgnoreCase(
                "a", PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent())
                .extracting(AssetMaster::getAssetId)
                .containsExactlyInAnyOrder("A001", "a002");
    }

    @Test
    @DisplayName("ページング: 1ページ3件の指定で正しく分割される")
    void ページング検証() {
        Page<AssetMaster> page = assetMasterRepository.findAll(
                PageRequest.of(0, 2, Sort.by("assetId").ascending()));

        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getTotalPages()).isEqualTo(2);
    }
}