package com.portfolio.assetmanagement.controller;

import com.portfolio.assetmanagement.service.AssetCsvService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AssetUploadController {

    private final AssetCsvService assetCsvService;

    public AssetUploadController(
            AssetCsvService assetCsvService) {
                this.assetCsvService = assetCsvService;
    }

    @GetMapping("/assets/upload")
    public String uploadPage() {
        return "assets-upload";
    }

    @PostMapping("/assets/upload")
    public String uploadCsv(
            MultipartFile file,
            RedirectAttributes redirectAttributes
    ) {
        assetCsvService.importCsv(file);

        // リダイレクト先に一時的なメッセージを渡す
        redirectAttributes.addFlashAttribute("messageCSV", "Upload Completed");
        return "redirect:/assets/upload";
    }
}
