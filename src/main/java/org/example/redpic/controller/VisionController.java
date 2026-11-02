package org.example.redpic.controller;

import org.example.redpic.common.Result;
import org.example.redpic.service.VisionService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/vision")
public class VisionController {
    private final VisionService visionService;

    public VisionController(VisionService visionService) {
        this.visionService = visionService;
    }

    @PostMapping("/chat")
    public Result<String> analyzeImage(@RequestParam("file") MultipartFile file,
                                       @RequestParam(value = "question", required = false, defaultValue = "请分析图片中的内容") String question) {
        String answer = visionService.analyzeImage(file, question);
        return Result.success(answer);
    }
}
