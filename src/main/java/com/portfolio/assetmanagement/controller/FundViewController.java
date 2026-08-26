package com.portfolio.assetmanagement.controller;

import com.portfolio.assetmanagement.entity.Fund;
import com.portfolio.assetmanagement.entity.FundNavHistory;
import com.portfolio.assetmanagement.repository.FundRepository;
import com.portfolio.assetmanagement.repository.FundNavHistoryRepository;
import com.portfolio.assetmanagement.service.FundService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Controller
public class FundViewController {

    private final FundRepository fundRepository;
    private final FundNavHistoryRepository historyRepository;

    public FundViewController(FundRepository fundRepository,
                              FundNavHistoryRepository historyRepository) {
        this.fundRepository = fundRepository;
        this.historyRepository = historyRepository;
    }

    @GetMapping("/funds/{id}")
    public String fundDetail(@PathVariable Long id, Model model) {

        Fund fund = fundRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fund not found: " + id));

        List<FundNavHistory> history =
                historyRepository.findByFundOrderByNavDateDesc(fund);
        model.addAttribute("fund", fund);
        model.addAttribute("history", history);

        return "fund-detail";
    }
}
