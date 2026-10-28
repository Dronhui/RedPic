package org.example.redpic.service.impl;

import org.example.redpic.config.AiProperties;
import org.example.redpic.dto.ChatReply;
import org.example.redpic.dto.ChatRequest;
import org.example.redpic.service.ChatService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ChatServiceImpl implements ChatService {
    private final ChatClient chatClient;
    private final AiProperties aiProperties;
    private final MessageWindowChatMemory aiChatMemory;

    public ChatServiceImpl(ChatClient.Builder chatClientBuilder,
                           AiProperties aiProperties,
                           MessageWindowChatMemory aiChatMemory) {
        this.chatClient = chatClientBuilder.build();
        this.aiProperties = aiProperties;
        this.aiChatMemory = aiChatMemory;
    }

    @Override
    public ChatReply chat(ChatRequest chatRequest) {
        String sessionId = chatRequest.sessionId();
        if (chatRequest.sessionId() == null||sessionId.isBlank()) {
            sessionId=UUID.randomUUID().toString();
        }

        var history=aiChatMemory.get(sessionId);
        ChatResponse answer = chatClient.prompt()
                .system(aiProperties.systemPrompt())
                .messages(history)
                .user(chatRequest.message())
                .call()
                .chatResponse();
        String aitextanswer=answer.getResult().getOutput().getText();
        if (aitextanswer == null || aitextanswer.trim().isEmpty()) {
            aitextanswer = "抱歉，网络开小差了，请稍后再试。";
        }
        aiChatMemory.add(sessionId, new UserMessage(chatRequest.message()));
        aiChatMemory.add(sessionId, new AssistantMessage(aitextanswer));
        return new ChatReply(sessionId,aitextanswer);
    }
}
