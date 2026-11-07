package org.example.redpic.controller;
import cn.dev33.satoken.annotation.SaCheckLogin;
import org.example.redpic.service.ChatService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/v1/chat")
@SaCheckLogin
public class ChatController {
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping(value = "/unified",consumes = MediaType.MULTIPART_FORM_DATA_VALUE,produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam(value = "question", defaultValue = "") String question,
            @RequestParam(value = "useRag", defaultValue = "false") boolean useRag,
            @RequestParam(value = "type", defaultValue = "default") String type,
            @RequestParam(value = "sessionId", defaultValue = "", required = false) String sessionId
    ) {
        return chatService.chat(image, question, useRag, type, sessionId);


    }
}