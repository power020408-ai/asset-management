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
import org.springframework.data.domain.Page;
import org.springframework.test.web.servlet.MvcResult;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

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
                        .param("assetId", "A001")
                        .param("assetType", "株式")
                        .param("assetName", "テスト")
                        .param("page", "1")
                        .param("searchAssetId",""))
                .andExpect(redirectedUrl("/assets/upload?searchAssetId=&page=1"))
                .andExpect(flash().attributeExists("messageGreen"));
    }

    private static AssetMaster asset(String assetId, String assetName, String assetType) {
        AssetMaster a = new AssetMaster();
        a.setAssetId(assetId);
        a.setAssetName(assetName);
        a.setAssetType(assetType);
        return a;
    }

    /** Pageable の offset/size に応じて all をスライスして返す、実物に近いページングのモック。 */
    private static Page<AssetMaster> slice(List<AssetMaster> all, Pageable pageable) {
        int from = (int) pageable.getOffset();
        int to = Math.min(from + pageable.getPageSize(), all.size());
        List<AssetMaster> content = from >= all.size() ? List.of() : all.subList(from, to);
        return new PageImpl<>(content, pageable, all.size());
    }

    @SuppressWarnings("unchecked")
    @Test
    void 検索なしの場合はid降順で3件ずつページ表示される() throws Exception {
        // Given: A1〜A5, B1 の6件。id はこの順で降順（A1 が最新 = DESC の先頭）
        List<AssetMaster> all = List.of(
                asset("A1", "", ""),
                asset("A2", "", ""),
                asset("A3", "", ""),
                asset("A4", "", ""),
                asset("A5", "", ""),
                asset("B1", "", ""));
        when(assetMasterRepository
                .findAll(any(Pageable.class)))
                .thenAnswer(inv -> slice(all, inv.getArgument(0)));

        MvcResult page0Result = mockMvc.perform(get("/assets/upload").param("page", "0"))
                .andExpect(status().isOk())
                .andExpect(view().name("assets-upload"))
                .andReturn();

        Page<AssetMaster> page0 = (Page<AssetMaster>)
                page0Result.getModelAndView().getModel().get("assetPage");
        assertEquals(List.of("A1", "A2", "A3"),
                page0.getContent().stream().map(AssetMaster::getAssetId).toList());
        Object searchAssetId = page0Result.getModelAndView().getModel().get("searchAssetId");
        assertTrue(searchAssetId == null || searchAssetId.toString().isBlank());

        MvcResult page1Result = mockMvc.
                perform(get("/assets/upload").param("page", "1"))
                .andExpect(status().isOk())
                .andReturn();
        Page<AssetMaster> page1 = (Page<AssetMaster>)
                page1Result.getModelAndView().getModel().get("assetPage");
        assertEquals(List.of("A4", "A5", "B1"),
                page1.getContent().stream().map(AssetMaster::getAssetId).toList());
    }

    @SuppressWarnings("unchecked")
    @Test
    void 検索ありの場合は一致した結果だけASC順でページ表示される() throws Exception {
        // Given: searchAssetId="A" に一致するのは A1〜A5（B1 は対象外）
        List<AssetMaster> matched = List.of(
                asset("A1", "", ""),
                asset("A2", "", ""),
                asset("A3", "", ""),
                asset("A4", "", ""),
                asset("A5", "", ""));
        when(assetMasterRepository
                .findByAssetIdContainingIgnoreCase(eq("A"), any(Pageable.class)))
                .thenAnswer(inv -> slice(matched, inv.getArgument(1)));

        MvcResult page0Result = mockMvc.perform(get("/assets/upload")
                        .param("searchAssetId", "A").param("page", "0"))
                .andExpect(status().isOk())
                .andReturn();
        Page<AssetMaster> page0 = (Page<AssetMaster>)
                page0Result.getModelAndView().getModel().get("assetPage");
        assertEquals(List.of("A1", "A2", "A3"),
                page0.getContent().stream().map(AssetMaster::getAssetId).toList());
        assertEquals("A", page0Result.getModelAndView().getModel().get("searchAssetId"));

        MvcResult page1Result = mockMvc.perform(get("/assets/upload")
                        .param("searchAssetId", "A").param("page", "1"))
                .andExpect(status().isOk())
                .andReturn();
        Page<AssetMaster> page1 = (Page<AssetMaster>)
                page1Result.getModelAndView().getModel().get("assetPage");
        assertEquals(List.of("A4", "A5"),
                page1.getContent().stream().map(AssetMaster::getAssetId).toList());
    }

    @Test
    void 空欄があっても表示の通り更新() throws Exception {
        // Given: searchAssetId="A" の一致件数は5件（A1〜A5）→ 3件/ページで有効ページは0,1のみ
        AssetMaster a4 = asset("A4", "", "");
        AssetMaster a5 = asset("A5", "", "");
        when(assetMasterRepository.findByAssetId("A4")).thenReturn(Optional.of(a4));
        when(assetMasterRepository.findByAssetId("A5")).thenReturn(Optional.of(a5));

        mockMvc.perform(post("/assets/master/update")
                        .param("assetId", "A4", "A5")
                        .param("assetType", null, "STOCK")
                        .param("assetName", "A4")
                        .param("searchAssetId", "A")
                        .param("page", "1"))
                .andExpect(redirectedUrl("/assets/upload?searchAssetId=A&page=1"))
                .andExpect(flash().attribute("messageGreen", "更新しました"));

        assertEquals("A4", a4.getAssetName());
        assertTrue(a4.getAssetType() == null || a4.getAssetType().isBlank());
        assertTrue(a5.getAssetName() == null || a5.getAssetName().isBlank());
        assertEquals("STOCK", a5.getAssetType());
        verify(assetMasterRepository).save(a4);
        verify(assetMasterRepository).save(a5);
    }

}