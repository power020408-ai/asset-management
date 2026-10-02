package com.portfolio.assetmanagement.controller;

import com.portfolio.assetmanagement.entity.AssetMaster;
import com.portfolio.assetmanagement.repository.AssetMasterRepository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

@Controller
public class AssetMasterController {
    private static final int PAGE_SIZE = 3;
    private final AssetMasterRepository assetMasterRepository;

    public AssetMasterController(
            AssetMasterRepository assetMasterRepository) {
        this.assetMasterRepository = assetMasterRepository;
    }

    @GetMapping("/assets/upload")
    public String uploadPage(
            @RequestParam(name = "searchAssetId", required = false) String searchAssetId,
            @RequestParam(name = "page", defaultValue = "0") int page,   // ★追加
            Model model) {
        if (page < 0) {          // URL手打ちで page=-1 が来ると例外になるため
            page = 0;
        }

        if (searchAssetId != null && !searchAssetId.isBlank()) {
            Pageable pageable = PageRequest.of(page, PAGE_SIZE,
                    Sort.by(Sort.Direction.ASC, "assetId"));
            Page<AssetMaster> assetPage =
                    assetMasterRepository.findByAssetIdContainingIgnoreCase
                            (searchAssetId, pageable);
            if (assetPage.isEmpty()) {
                model.addAttribute("messageRed", "該当する asset_id がありません");
                model.addAttribute("assetPage", null);
            } else {
                model.addAttribute("assetPage", assetPage);
            }
        } else {
            Pageable pageable = PageRequest.of(page, PAGE_SIZE,
                    Sort.by(Sort.Direction.DESC, "id"));
            Page<AssetMaster> assetPage = assetMasterRepository.findAll(pageable);
            model.addAttribute("assetPage", assetPage);
        }
        model.addAttribute("page", page);
        model.addAttribute("searchAssetId", searchAssetId); // 入力欄に残す

        return "assets-upload";
    }

    /**
     * 更新：POST /assets/master/update
     */
    @PostMapping("/assets/master/update")
    @Transactional          // （3行まとめて成功／失敗）
    public String update(@RequestParam List<String> assetId,
                         @RequestParam List<String> assetType,
                         @RequestParam List<String> assetName,
                         @RequestParam(name = "searchAssetId", required = false) String searchAssetId,
                         @RequestParam(name = "page", defaultValue = "0") int page,
                         RedirectAttributes redirectAttributes) {

        record UpdateList(String assetId, String assetType, String assetName) {
        }

        for (int i = 0; i < assetId.size(); i++) {
            if (i < assetType.size()) {
                assetType.set(i, Optional.ofNullable(assetType.get(i)).orElse(""));
            } else {assetType.add("");}
            if (i < assetName.size()) {
                assetName.set(i, Optional.ofNullable(assetName.get(i)).orElse(""));
            } else {assetName.add("");}
        }
        List<UpdateList> updateList = IntStream.range(0,  assetId.size())
                .mapToObj(i -> new UpdateList(assetId.get(i), assetType.get(i), assetName.get(i)))
                .toList();

        updateList.forEach(rec -> {
            // 1. IDでDBから既存ユーザーを検索
            assetMasterRepository.findByAssetId(rec.assetId()).ifPresent(dbRec -> {
                // 2. 取得したエンティティのフィールドを更新
                dbRec.setAssetType(rec.assetType());
                dbRec.setAssetName(rec.assetName());

                // 3. 保存（※JPAの@Transactional内であれば save の呼び出しは省略可能であるが、明示的に書く）
                assetMasterRepository.save(dbRec);
            });
        });

        redirectAttributes.addAttribute("searchAssetId", searchAssetId);
        redirectAttributes.addAttribute("page", page);
        redirectAttributes.addFlashAttribute(
                "messageGreen", "更新しました");
        return "redirect:/assets/upload";
    }
}


