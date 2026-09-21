package com.portfolio.assetmanagement.controller;

import com.portfolio.assetmanagement.repository.AssetRepository;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Autowired;

//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;

//import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NavStartController.class)
class NavStartControllerWebTest {

    @Autowired
    MockMvc mockMvc;

    // Controller が要求する3つの依存を、すべて「身代わり」として登録する
    @MockitoBean JobOperator jobOperator;
    @MockitoBean Job navJob;
    @MockitoBean AssetRepository assetRepository;

    @Test
    void POST_nav_startは資産がなければアップロード画面へリダイレクトする() throws Exception {
        when(assetRepository.existsByNavDate(any(LocalDate.class)))
                .thenReturn(false);

        mockMvc.perform(post("/nav/start"))
                .andExpect(status().is3xxRedirection())      // リダイレクトか
                .andExpect(redirectedUrl("/assets/upload"))  // どこへ戻るか
                .andExpect(flash().attribute(
                        "messageNAV", "Today's Asset CSV not uploaded"));
    }
}