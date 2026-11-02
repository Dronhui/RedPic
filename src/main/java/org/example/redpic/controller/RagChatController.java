package org.example.redpic.controller;

import org.example.redpic.common.Result;
import org.example.redpic.dto.RagChatRequest;
import org.example.redpic.service.RagChatService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rag")
public class RagChatController {
    final RagChatService ragChatService;
    public RagChatController(RagChatService ragChatService) {
        this.ragChatService = ragChatService;
    }
    @PostMapping("/chat")
    public Result<String> chatWithRag(@RequestBody RagChatRequest ragChatRequest) {
        String answer = ragChatService.chatWithRag(ragChatRequest.question());
        return Result.success(answer);
    }


}
