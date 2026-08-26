package com.portfolio.assetmanagement.batch;

import com.portfolio.assetmanagement.entity.Fund;
import com.portfolio.assetmanagement.repository.FundRepository;
import com.portfolio.assetmanagement.service.FundService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class NavCalculationTasklet implements Tasklet {
    private static final Logger log = LoggerFactory.getLogger(NavCalculationTasklet.class);
    private final FundRepository fundRepository;
    private final FundService fundService;

    public NavCalculationTasklet(FundRepository fundRepository, FundService fundService) {
        this.fundRepository = fundRepository;
        this.fundService = fundService;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {

        List<Fund> funds = fundRepository.findAll();

        for (Fund fund : funds) {
            fundService.calculateNav(fund.getFundId(),LocalDate.now());
            log.info("NAV calculated for Fund: {}", fund.getName());
        }

        return RepeatStatus.FINISHED;
    }
}
