package com.portfolio.assetmanagement.controller;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
//import org.springframework.batch.core.launch.JobLauncher; // Depreciated
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;

@Controller
public class AssetUploadController {
    //private final JobLauncher jobLauncher;
    private final JobOperator jobOperator;
    private final Job importUserJob;


    public AssetUploadController(
            //JobLauncher jobLauncher,
            JobOperator jobOperator,
            Job importUserJob

            ) {
        //this.jobLauncher = jobLauncher;
        this.jobOperator = jobOperator;
        this.importUserJob = importUserJob;

    }

    @GetMapping("/assets/upload")
    public String uploadPage() {
        return "assets-upload";
    }

    @PostMapping("/assets/upload")
    public String uploadCsv(
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes) throws Exception {
                if (file.isEmpty()) {
                    redirectAttributes.addFlashAttribute("messageCSV", "ファイルを選択してください。");
                    return "redirect:/assets/upload";
                }

                // 1. 一時ファイルとして保存する
                File tempFile = File.createTempFile("upload_", "_" + file.getOriginalFilename());
                file.transferTo(tempFile);
                JobParameters params = new JobParametersBuilder()
                    .addString("filePath", tempFile.getAbsolutePath()) // ファイルパスを登録
                    .addLong("time", System.currentTimeMillis())  // 毎回違う値にする
                    .toJobParameters();

                // 2. ジョブの実行（デフォルトでは同期実行され、終了までここでブロックされます）
                //JobExecution jobExecution = jobLauncher.run(importUserJob, params);
                JobExecution jobExecution = jobOperator.start(importUserJob, params);

                // 3. ジョブの実行結果（BatchStatus）をチェックして画面メッセージを設定
                if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
                    redirectAttributes.addFlashAttribute("messageCSV", "CSV Upload Completed");
                } else if (jobExecution.getStatus() == BatchStatus.FAILED) {
                    redirectAttributes.addFlashAttribute("messageCSV", "CSV Upload Failed");
                } else {
                    redirectAttributes.addFlashAttribute("messageCSV", "Upload Status: " + jobExecution.getStatus());
                }

                // 一時ファイルの削除
                tempFile.delete();

                return "redirect:/assets/upload";
    }
}
