package org.example.redpic.controller;

import org.example.redpic.common.Result;
import org.example.redpic.dto.ChatRequest;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
@RestController
public class ChatController {

    private final ChatClient chatClient;

    public ChatController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @PostMapping("/api/v1/chat")
    public Result<String> chat(
        @Valid
        @RequestBody
        ChatRequest request
    ){
        String answer = chatClient.prompt()
                .user(request.message())
                .call()
                .content();
        return Result.success(answer);
    }
}