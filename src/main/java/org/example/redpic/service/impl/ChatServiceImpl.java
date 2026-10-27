package org.example.redpic.service.impl;

import org.example.redpic.config.AiProperties;
import org.example.redpic.service.ChatService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class ChatServiceImpl implements ChatService {
    private final ChatClient chatClient;
    private final AiProperties aiProperties;

    public ChatServiceImpl(ChatClient.Builder chatClientBuilder, AiProperties aiProperties) {
        this.chatClient = chatClientBuilder.build();
        this.aiProperties = aiProperties;
    }

    @Override
    public String chat(String message) {
        return chatClient.prompt()
                .system(aiProperties.systemPrompt())
                .user(message)
                .call()
                .content();
    }
}
