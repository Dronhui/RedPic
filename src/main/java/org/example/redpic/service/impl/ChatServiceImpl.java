package org.example.redpic.service.impl;

import org.example.redpic.config.AiProperties;
import org.example.redpic.dto.ChatReply;
import org.example.redpic.dto.ChatRequest;
import org.example.redpic.service.ChatService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
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
        this.chatClient = chatClientBuilder.defaultAdvisors(MessageChatMemoryAdvisor.builder(aiChatMemory).build()).build();
        this.aiProperties = aiProperties;
        this.aiChatMemory = aiChatMemory;
    }

    @Override
    public ChatReply chat(ChatRequest chatRequest) {
        String inputId = chatRequest.sessionId();
        String finalSessionId = (inputId == null || inputId.isBlank()) ? UUID.randomUUID().toString() : inputId;


        ChatResponse answer = chatClient.prompt()
                .system(aiProperties.systemPrompt())
                .user(chatRequest.message())
                .advisors(a->a.param(ChatMemory.CONVERSATION_ID,finalSessionId))
                .call()
                .chatResponse();
        String aitextanswer=answer.getResult().getOutput().getText();
        if (aitextanswer == null || aitextanswer.trim().isEmpty()) {
            aitextanswer = "抱歉，网络开小差了，请稍后再试。";
        }
        return new ChatReply(finalSessionId,aitextanswer);
    }
}
