package com.portfolio.assetmanagement.batch;

import javax.sql.DataSource;

import com.portfolio.assetmanagement.entity.AssetCsv;
import com.portfolio.assetmanagement.entity.AssetTable;
import com.portfolio.assetmanagement.service.AssetItemProcessor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.database.JdbcBatchItemWriter;
import org.springframework.batch.infrastructure.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
//import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class AssetCsvBatchJob {

    private static final Logger log = LoggerFactory.getLogger(AssetCsvBatchJob.class);
    // tag::readerwriterprocessor[]

    @Bean
    @StepScope
    public FlatFileItemReader<AssetCsv> reader(
            @Value("#{jobParameters['filePath']}") String filePath
    ) {
        return new FlatFileItemReaderBuilder<AssetCsv>()
                .name("assetItemReader")
                .resource(new FileSystemResource(filePath)) // FileSystemResource を使用
                .delimited()
                .names("fundIdStr", "assetIdStr", "navDateStr", "assetName", "amountStr")
                .targetType(AssetCsv.class)
                .build();
    }

    @Bean
    public AssetItemProcessor processor() {
        return new AssetItemProcessor();
    }

    @Bean
    public JdbcBatchItemWriter<AssetTable> writer(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<AssetTable>()
                .sql("""
                        INSERT INTO assets
                          	(asset_id, amount, asset_name, fund_id , nav_date)
                        VALUES
                          	(:assetIdStr, :amount, :assetName, :fundId , :navDate)
                        ON CONFLICT(asset_id, fund_id , nav_date)
                        DO UPDATE SET
                          	asset_id = excluded.asset_id,
                        	fund_id = excluded.fund_id,
                        	nav_date = excluded.nav_date,
                            asset_name = excluded.asset_name,
                        	amount = excluded.amount;
                        """ )
                .dataSource(dataSource)
                .beanMapped()
                .build();
    }
    // end::readerwriterprocessor[]

    // tag::jobstep[]
    @Bean
    public Job importUserJob(JobRepository jobRepository, Step step1,
                             JobCompletionNotificationListener listener) {
        return new JobBuilder(jobRepository)
                .listener(listener)
                .start(step1)
                .build();
    }

    @Bean
    public Step step1(JobRepository jobRepository,
                      PlatformTransactionManager transactionManager,
                      FlatFileItemReader<AssetCsv> reader,
                      AssetItemProcessor processor,
                      JdbcBatchItemWriter<AssetTable> writer) {

        return new StepBuilder(jobRepository)
			.<AssetCsv, AssetTable>chunk(2)
			.transactionManager(transactionManager)
			.reader(reader)
			.processor(processor)
			.writer(writer)
			.build();
}
// end::jobstep[]
}
