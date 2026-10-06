package com.portfolio.assetmanagement.controller;

import com.portfolio.assetmanagement.entity.AssetMaster;
import com.portfolio.assetmanagement.repository.AssetMasterRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.mockito.ArgumentCaptor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.data.domain.Page;
import org.springframework.test.web.servlet.MvcResult;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;

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
                .andExpect(view().name("assets-upload"));

        // ArgumentCaptor - コントローラが「何を」要求したかを捕まえる
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(assetMasterRepository).findAll(captor.capture());

        Pageable requested = captor.getValue();
        assertEquals(0, requested.getPageNumber());   // 1ページ目
        assertEquals(3, requested.getPageSize());     // 3件

        Sort.Order order = requested.getSort().getOrderFor("id");
        assertNotNull(order);                          // id で
        assertEquals(Sort.Direction.DESC, order.getDirection()); // 降順
    }

    @Test
    void 更新後はページ番号を保ったまま画面へ戻る() throws Exception {
        when(assetMasterRepository.findByAssetId("A001"))
                .thenReturn(Optional.of(new AssetMaster()));

        MvcResult result = mockMvc.perform(post("/assets/master/update")
                        .param("assetId", "A001")
                        .param("assetType", "株式")
                        .param("assetName", "テスト")
                        .param("page", "1")
                        .param("searchAssetId",""))
                .andExpect(status().is3xxRedirection()) // リダイレクトが発生したか検証
                .andExpect(flash().attributeExists("messageGreen"))
                .andReturn();

        String redirectedUrl = result.getResponse().getRedirectedUrl();
        assertThat(redirectedUrl).contains("page=1");
    }

    private static AssetMaster asset(String assetId, String assetName, String assetType) {
        AssetMaster a = new AssetMaster();
        a.setAssetId(assetId);
        a.setAssetName(assetName);
        a.setAssetType(assetType);
        return a;
    }

    /** Pageable の offset/size に応じて all をスライスして返す、実物に近いページングのモック。 */
    private static<T> Page<T> slice(List<T> all, Pageable pageable) {
        int from = (int) pageable.getOffset();
        int to = Math.min(from + pageable.getPageSize(), all.size());
        List<T> content = from >= all.size() ? List.of() : all.subList(from, to);
        return new PageImpl<>(content, pageable, all.size());
    }

    @SuppressWarnings("unchecked")
    private static Page<AssetMaster> assetPageOf(MvcResult result) {
        return (Page<AssetMaster>) result.getModelAndView().getModel().get("assetPage");
    }

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

        Page<AssetMaster> page0 = assetPageOf(page0Result);
        assertEquals(List.of("A1", "A2", "A3"),
                page0.getContent().stream().map(AssetMaster::getAssetId).toList());
        Object searchAssetId = page0Result.getModelAndView().getModel().get("searchAssetId");
        assertTrue(searchAssetId == null || searchAssetId.toString().isBlank());

        MvcResult page1Result = mockMvc.
                perform(get("/assets/upload").param("page", "1"))
                .andExpect(status().isOk())
                .andReturn();
        Page<AssetMaster> page1 = assetPageOf(page1Result);
        assertEquals(List.of("A4", "A5", "B1"),
                page1.getContent().stream().map(AssetMaster::getAssetId).toList());
    }

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
        Page<AssetMaster> page0 = assetPageOf(page0Result);
        assertEquals(List.of("A1", "A2", "A3"),
                page0.getContent().stream().map(AssetMaster::getAssetId).toList());
        assertEquals("A", page0Result.getModelAndView().getModel().get("searchAssetId"));

        MvcResult page1Result = mockMvc.perform(get("/assets/upload")
                        .param("searchAssetId", "A").param("page", "1"))
                .andExpect(status().isOk())
                .andReturn();
        Page<AssetMaster> page1 = page1 = assetPageOf(page1Result);
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

        MvcResult result = mockMvc.perform(post("/assets/master/update")
                        .param("assetId", "A4", "A5")
                        .param("assetType", "", "STOCK")
                        .param("assetName", "A4")
                        .param("searchAssetId", "A")
                        .param("page", "1"))
                .andExpect(status().is3xxRedirection()) // リダイレクトが発生したか検証
                .andExpect(flash().attribute("messageGreen", "更新しました"))
                .andReturn();

        String redirectedUrl = result.getResponse().getRedirectedUrl();
        assertThat(redirectedUrl).contains("page=1");


        assertEquals("A4", a4.getAssetName());
        assertTrue(a4.getAssetType() == null || a4.getAssetType().isBlank());
        assertTrue(a5.getAssetName() == null || a5.getAssetName().isBlank());
        assertEquals("STOCK", a5.getAssetType());
        verify(assetMasterRepository).save(a4);
        verify(assetMasterRepository).save(a5);
    }

    @Test
    void ページにマイナス1が来ても0ページ目として扱う() throws Exception {
        when(assetMasterRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(new AssetMaster()),
                        PageRequest.of(0, 3), 7));   // 全7件

        mockMvc.perform(get("/assets/upload")
                        .param("page", "-1"))
                .andExpect(status().isOk())
                .andExpect(view().name("assets-upload"))
                .andExpect(model().attribute("page", 0));   // 画面側にも0が渡る

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(assetMasterRepository).findAll(captor.capture());
        assertEquals(0, captor.getValue().getPageNumber());  // 丸められた
    }

    @Test
    void 検索0件ならメッセージを出して一覧を出さない() throws Exception {
        when(assetMasterRepository.findByAssetIdContainingIgnoreCase(eq("ZZZ"), any(Pageable.class)))
                .thenReturn(Page.empty());

        mockMvc.perform(get("/assets/upload").param("searchAssetId", "ZZZ"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("assetPage", nullValue()))
                .andExpect(model().attribute("messageRed", "該当する asset_id がありません"))
                // 「一覧を出さない」を本当に確かめる：name=\"assetType\""は一覧の中にしか無い
                .andExpect(content().string(not(containsString("name=\"assetType\""))));

    }

    @Test
    void 存在しない銘柄コードの行は保存しない() throws Exception {
        when(assetMasterRepository.findByAssetId("XXXX")).thenReturn(Optional.empty());

        mockMvc.perform(post("/assets/master/update")
                        .param("assetId", "XXXX")
                        .param("assetType", "株式")
                        .param("assetName", "テスト"))
                .andExpect(status().is3xxRedirection());

        verify(assetMasterRepository).findByAssetId("XXXX");
        verify(assetMasterRepository, never()).save(any(AssetMaster.class));
    }


}