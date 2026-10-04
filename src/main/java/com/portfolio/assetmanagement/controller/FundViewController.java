package com.portfolio.assetmanagement.controller;

import com.portfolio.assetmanagement.entity.Fund;
import com.portfolio.assetmanagement.entity.FundNavHistory;
import com.portfolio.assetmanagement.repository.FundRepository;
import com.portfolio.assetmanagement.repository.FundNavHistoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class FundViewController {
    private static final int PAGE_SIZE = 5;
    private final FundRepository fundRepository;
    private final FundNavHistoryRepository historyRepository;

    public FundViewController(FundRepository fundRepository,
                              FundNavHistoryRepository historyRepository) {
        this.fundRepository = fundRepository;
        this.historyRepository = historyRepository;
    }

    @GetMapping("/funds/{id}")
    public String fundDetail(@PathVariable Long id,
                             @RequestParam(name = "page", defaultValue = "0") int page,   // ★追加
                             Model model) {
        if (page < 0) {
            page = 0;
        }
        Fund fund = fundRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fund not found: " + id));

        Pageable pageable = PageRequest.of(page, PAGE_SIZE,
                Sort.by(Sort.Direction.DESC, "navDate"));
        Page<FundNavHistory> history = historyRepository.findByFund(fund, pageable); //  .findByFund(fund, pageable);
        if (page < 0) page = 0;
        // 取得後に、範囲外なら最終ページへ寄せる（任意）
        if (page >= history.getTotalPages() && history.getTotalPages() > 0) {
            page = history.getTotalPages() - 1;
        }
        model.addAttribute("fund", fund);
        model.addAttribute("history", history);

        return "fund-detail";
    }
}
