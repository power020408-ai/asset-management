package com.portfolio.assetmanagement.scheduler;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;

import org.springframework.batch.core.launch.JobOperator;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class NavScheduler {

    private final JobOperator jobOperator;
    private final Job navJob;

    public NavScheduler(JobOperator jobOperator, Job navJob) {
        this.jobOperator = jobOperator;
        this.navJob = navJob;
    }

    // 毎日 01:00 に NAV を計算 - 秒：0, 分：0, 時：20, 日：*(毎日), 月：*(毎月), 曜日：*(毎日)
    @Scheduled(cron = "0 0 20 * * *")
    public void startNavJob() {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())  // 毎回違う値にする
                    .toJobParameters();

            // ジョブの実行
            JobExecution jobExecution = jobOperator.start(navJob, params);

            System.out.println("Job Status : " + jobExecution.getStatus());

        } catch (Exception e) {
            // エラー時はログに出力する
            System.err.println("NAV Job failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}