package com.portfolio.assetmanagement.batch;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class NavBatchJob {

    private final NavCalculationTasklet navCalculationTasklet;

    public NavBatchJob(NavCalculationTasklet navCalculationTasklet) {
        this.navCalculationTasklet = navCalculationTasklet;
    }

    @Bean
    public Job navJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new JobBuilder("navJob", jobRepository)
                .start(navStep(jobRepository, transactionManager))
                .build();
    }

    @Bean
    public Step navStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("navStep", jobRepository)
                .tasklet(navCalculationTasklet, transactionManager)
                .build();
    }
}
