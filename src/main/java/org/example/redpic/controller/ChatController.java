package org.example.redpic.controller;

import org.example.redpic.common.Result;
import org.example.redpic.dto.ChatRequest;
import org.example.redpic.service.ChatService;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
@RestController
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/api/v1/chat")
    public Result<String> chat(
        @Valid
        @RequestBody
        ChatRequest request
    ){
        String answer = chatService.chat(request.message());
        return Result.success(answer);
    }
}