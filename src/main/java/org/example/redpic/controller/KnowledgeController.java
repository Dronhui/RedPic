package org.example.redpic.controller;

import org.example.redpic.common.Result;
import org.example.redpic.service.KnowledgeBaseService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeController {
    private final KnowledgeBaseService knowledgeBaseService;

    public KnowledgeController(KnowledgeBaseService knowledgeBaseService) {
        this.knowledgeBaseService = knowledgeBaseService;
    }

    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        knowledgeBaseService.importDocument(file);
        return Result.success("文件导入成功");
    }

    @GetMapping("/read")
    public Result<String> readBook(@RequestParam("fileName") String fileName) {
        ClassPathResource resource = new ClassPathResource("data/" + fileName);
        if (!resource.exists()) {
            return Result.bizError("文件不存在");
        }
        try (InputStream is = resource.getInputStream()) {
            String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            return Result.success(content);
        } catch (Exception e) {
            return Result.error(500, "读取文件失败" + e.getMessage());
        }

    }
}


