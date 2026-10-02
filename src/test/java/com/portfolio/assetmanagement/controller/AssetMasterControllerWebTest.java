package com.portfolio.assetmanagement.controller;

import com.portfolio.assetmanagement.entity.AssetMaster;
import com.portfolio.assetmanagement.repository.AssetMasterRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AssetMasterController.class)   // 対象を絞ると依存の用意が減る
class AssetMasterControllerWebTest {

    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    AssetMasterRepository assetMasterRepository;

    @Test
    void 初期表示は1ページ目を3件で表示する() throws Exception {
        when(assetMasterRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(new AssetMaster()),
                        PageRequest.of(0, 3), 7));   // 全7件

        mockMvc.perform(get("/assets/upload"))
                .andExpect(status().isOk())
                .andExpect(view().name("assets-upload"))
                .andExpect(model().attributeExists("assetPage"));
    }

    @Test
    void 更新後はページ番号を保ったまま画面へ戻る() throws Exception {
        when(assetMasterRepository.findByAssetId("A001"))
                .thenReturn(Optional.of(new AssetMaster()));

        mockMvc.perform(post("/assets/master/update")
                        .param("assetId", "A001").param("assetType", "株式")
                        .param("assetName", "テスト").param("page", "1")
                        .param("searchAssetId",""))
                .andExpect(redirectedUrl("/assets/upload?searchAssetId=&page=1"))
                .andExpect(flash().attributeExists("messageGreen"));
    }
}