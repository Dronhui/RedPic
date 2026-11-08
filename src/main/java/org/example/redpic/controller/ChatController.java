package org.example.redpic.controller;
import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.redpic.common.Result;
import org.example.redpic.entity.ChatHistory;
import org.example.redpic.mapper.ChatHistoryMapper;
import org.example.redpic.service.ChatService;
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
    private final ChatHistoryMapper chatHistoryMapper;
    public ChatController(ChatService chatService, ChatHistoryMapper chatHistoryMapper) {
        this.chatService = chatService;
        this.chatHistoryMapper = chatHistoryMapper;
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
    @GetMapping("/sessions")
    public Result<List<Map<String,Object>>> getSessions() {
        String userId = cn.dev33.satoken.stp.StpUtil.getLoginIdAsString();
        List<ChatHistory> chatHistoriesList = chatHistoryMapper.selectList(new QueryWrapper<ChatHistory>().eq("user_id", userId)
                .orderByAsc("create_time")
        );
        Map<String, Map<String, Object>> sessionMap = new LinkedHashMap<>();
        for (ChatHistory chatHistory : chatHistoriesList) {
            if (!sessionMap.containsKey(chatHistory.getSessionId())) {
                Map<String, Object> sessioninfo = new HashMap<>();
                sessioninfo.put("sessionId", chatHistory.getSessionId());
                String title = chatHistory.getUserMessage();
                sessioninfo.put("title", title.length() >= 15 ? title.substring(0, 15) + "..." : title);
                sessioninfo.put("createTime", chatHistory.getCreateTime());
                sessionMap.put(chatHistory.getSessionId(), sessioninfo);


            } else {
                sessionMap.get(chatHistory.getSessionId()).put("createTime", chatHistory.getCreateTime());
            }

        }
        List<Map<String, Object>> resultList = new ArrayList<>(sessionMap.values());
        Collections.reverse(resultList);
        return Result.success(resultList);
    }
        @GetMapping("/history")
        public Result<List<Map<String,String>>> getHistory(@RequestParam("sessionId") String sessionId) {
            return Result.success(chatService.getSessionHistory(sessionId));
        }
}