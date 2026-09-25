package com.portfolio.assetmanagement.controller;

import com.portfolio.assetmanagement.repository.AssetRepository;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
public class NavStartController {

    private final JobOperator jobOperator;
    private final Job navJob;
    private final AssetRepository assetRepository;

    public NavStartController(JobOperator jobOperator, Job navJob,
            AssetRepository assetRepository) {
        this.jobOperator = jobOperator;
        this.navJob = navJob;
        this.assetRepository = assetRepository;
    }

    @PostMapping("/nav/start")
    public String startNavJob(RedirectAttributes redirectAttributes) throws Exception {
        LocalDate navDate = LocalDate.now();
        if (!assetRepository.existsByNavDate(navDate)) {
            redirectAttributes.addFlashAttribute("messageNAV",
                    "Today's Asset CSV not uploaded!");
            return "redirect:/assets/upload";
        }

        JobParameters params = new JobParametersBuilder()
                .addString("navDate", navDate.toString())
                .addLong("time", System.currentTimeMillis())  // 毎回違う値にする
                .toJobParameters();

        JobExecution jobExecution = jobOperator.start(navJob, params);

        // ジョブの実行結果（BatchStatus）をチェックして画面メッセージを設定
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            redirectAttributes.addFlashAttribute("messageNAV", "NAV Calculation Completed");
        } else if (jobExecution.getStatus() == BatchStatus.FAILED) {
            redirectAttributes.addFlashAttribute("messageNAV", "NAV Calculation Failed");
        } else {
            redirectAttributes.addFlashAttribute("messageNAV", "Calc Status: " + jobExecution.getStatus());
        }

        return "redirect:/assets/upload";
    }
}