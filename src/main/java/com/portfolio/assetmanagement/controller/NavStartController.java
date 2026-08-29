package com.portfolio.assetmanagement.controller;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class NavStartController {

    private final JobOperator jobOperator;
    private final Job navJob;


    public NavStartController(JobOperator jobOperator, Job navJob) {
        this.jobOperator = jobOperator;
        this.navJob = navJob;
    }

    @PostMapping("/nav/start")
    public String startNavJob(RedirectAttributes redirectAttributes) throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())  // 毎回違う値にする
                .toJobParameters();

        JobExecution jobExecution = jobOperator.start(navJob, params);


        // リダイレクト先に一時的なメッセージを渡す
        //    redirectAttributes.addFlashAttribute("messageNAV",
        //            "NAV Calculation Completed");
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