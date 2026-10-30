package org.example.redpic.controller;

import org.example.redpic.common.Result;
import org.example.redpic.service.KnowledgeBaseService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
}
