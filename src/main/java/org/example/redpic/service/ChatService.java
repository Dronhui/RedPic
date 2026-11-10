package org.example.redpic.service;

import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

public interface ChatService {
    Flux<String> chat(MultipartFile image, String question, boolean useRag, String type, String sessionId);

    List<Map<String, String>> getSessionHistory(String sessionId);
    List<Map<String, Object>> getUserSessions();
}
