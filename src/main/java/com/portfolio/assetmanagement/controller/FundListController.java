package com.portfolio.assetmanagement.controller;

import com.portfolio.assetmanagement.entity.Fund;
import com.portfolio.assetmanagement.repository.FundRepository;
import org.springframework.data.domain.Sort;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;

@Controller
//@RequestMapping("/funds")
public class FundListController {

    private final FundRepository fundRepository;

    public FundListController(FundRepository fundRepository) {
        this.fundRepository = fundRepository;
    }

    @GetMapping("/funds")
    public String showFunds(Model model) {
        model.addAttribute("funds",
                fundRepository.findAll(Sort.by(Sort.Direction.ASC, "fundId")));
        return "fund-list";
    }
}
