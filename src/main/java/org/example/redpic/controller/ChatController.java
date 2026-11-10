package org.example.redpic.controller;
import cn.dev33.satoken.annotation.SaCheckLogin;
import org.example.redpic.common.Result;
import org.example.redpic.service.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import java.util.*;

@RestController
@RequestMapping("/api/v1/chat")
@SaCheckLogin
public class ChatController {
    private final ChatService chatService;
    private static final Logger log = LoggerFactory.getLogger(ChatController.class);
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
        return chatService.chat(image, question, useRag, type, sessionId).onErrorResume(e -> {                       // ★ 出口兜底
            log.error("chat 流式异常", e);
            return Flux.just("同志，讲解过程中遇到了点问题，请稍后再试。");
        });
    }
    @GetMapping("/sessions")
    public Result<List<Map<String,Object>>> getUserSessions() {
        return Result.success(chatService.getUserSessions());
    }
    @GetMapping("/history")
    public Result<List<Map<String,String>>> getHistory(@RequestParam("sessionId") String sessionId) {
            return Result.success(chatService.getSessionHistory(sessionId));
        }
}