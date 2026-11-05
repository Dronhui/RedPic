package org.example.redpic.service;

import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

public interface ChatService {
    Flux<String> chat(MultipartFile image, String question, boolean useRag, String type, String sessionId);
}
