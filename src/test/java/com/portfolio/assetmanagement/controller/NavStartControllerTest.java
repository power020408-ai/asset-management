package com.portfolio.assetmanagement.controller;

import com.portfolio.assetmanagement.repository.AssetRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NavStartControllerTest {

    @Mock JobOperator jobOperator;
    @Mock Job navJob;
    @Mock AssetRepository assetRepository;
    @Mock RedirectAttributes redirectAttributes;

    @InjectMocks
    NavStartController controller;

    @Test
    void 今日の資産がなければジョブを起動せずアップロード画面へ戻る() throws Exception {
        // Arrange：今日の資産は「無い」と教える
        when(assetRepository.existsByNavDate(any(LocalDate.class)))
                .thenReturn(false);

        // Act
        String view = controller.startNavJob(redirectAttributes);

        // Assert：メッセージを出して、アップロード画面へリダイレクト
        assertEquals("redirect:/assets/upload", view);
        verify(redirectAttributes)
                .addFlashAttribute("messageNAV", "Today's Asset CSV not uploaded");

        // ★ 門番が効いた証拠：ジョブは一切起動されていない
        verifyNoInteractions(jobOperator);
    }

    @Test
    void ジョブが完了したら完了メッセージを出す() throws Exception {
        // Arrange
        when(assetRepository.existsByNavDate(any(LocalDate.class)))
                .thenReturn(true);

        JobExecution execution = mock(JobExecution.class);
        when(execution.getStatus()).thenReturn(BatchStatus.COMPLETED);
        when(jobOperator.start(eq(navJob), any(JobParameters.class)))
                .thenReturn(execution);

        // Act
        String view = controller.startNavJob(redirectAttributes);

        // Assert
        assertEquals("redirect:/assets/upload", view);
        verify(redirectAttributes)
                .addFlashAttribute("messageNAV", "NAV Calculation Completed");
    }

    @Test
    void ジョブが失敗したら失敗メッセージを出す() throws Exception {
        when(assetRepository.existsByNavDate(any(LocalDate.class)))
                .thenReturn(true);

        JobExecution execution = mock(JobExecution.class);
        when(execution.getStatus()).thenReturn(BatchStatus.FAILED);
        when(jobOperator.start(eq(navJob), any(JobParameters.class)))
                .thenReturn(execution);

        String view = controller.startNavJob(redirectAttributes);

        assertEquals("redirect:/assets/upload", view);
        verify(redirectAttributes)
                .addFlashAttribute("messageNAV", "NAV Calculation Failed");
    }
}