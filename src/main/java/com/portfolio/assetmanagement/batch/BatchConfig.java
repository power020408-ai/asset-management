package com.portfolio.assetmanagement.batch;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BatchConfig {
    // Spring Boot が Batch-JDBC の設定を自動構成するため、
    // @EnableBatchProcessing は付けない（付けるとDB永続化が無効化される）
}
