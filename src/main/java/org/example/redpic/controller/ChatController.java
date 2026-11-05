package org.example.redpic.controller;

import org.example.redpic.common.Result;
import org.example.redpic.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {
    private ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping(value = "/unified",consumes = MediaType.MULTIPART_FORM_DATA_VALUE,produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam(value = "question", defaultValue = "") String question,
            @RequestParam(value = "useRag", defaultValue = "false") boolean useRag,
            @RequestParam(value = "type", defaultValue = "default") String type,
            @RequestParam(value = "session", defaultValue = "", required = false) String session
    ) {
        return chatService.chat(image, question, useRag, type, session);


    }
}