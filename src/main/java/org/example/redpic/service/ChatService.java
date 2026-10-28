package org.example.redpic.service;

import org.example.redpic.dto.ChatReply;
import org.example.redpic.dto.ChatRequest;

public interface ChatService {
    ChatReply chat(ChatRequest chatRequest);
}
