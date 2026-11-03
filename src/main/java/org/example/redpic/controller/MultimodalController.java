package org.example.redpic.controller;

import org.example.redpic.common.Result;
import org.example.redpic.service.MultimodalService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/multimodal")
public class MultimodalController {
    private final MultimodalService multimodalService;
    public MultimodalController(MultimodalService multimodalService) {
        this.multimodalService = multimodalService;
    }
    @PostMapping("/chat")
    public Result<String> chatWithImage(@RequestParam(value = "image",required = false) MultipartFile image,
                                        @RequestParam(value = "question",defaultValue = "请讲解一下这个遗址的历史背景") String question,
                                        @RequestParam(value = "sessionId",defaultValue = "",required = false) String sessionId
                                       ) {
        String answer = multimodalService.chatWithImage(image, question,sessionId);
        return Result.success(answer);
    }
}
