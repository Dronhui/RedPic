package org.example.redpic.service.impl;

import org.example.redpic.config.AiPromptsProperties;
import org.example.redpic.dto.ChatReply;
import org.example.redpic.dto.ChatRequest;
import org.example.redpic.service.ChatService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.UUID;

@Service
public class ChatServiceImpl implements ChatService {
    private final ChatClient chatClient;
    private final AiPromptsProperties aiPromptsProperties;
    private final MessageWindowChatMemory aiChatMemory;

    public ChatServiceImpl(@Qualifier("memoryChatClient") ChatClient chatClient,
                           AiPromptsProperties aiPromptsProperties,
                           MessageWindowChatMemory aiChatMemory) {
        this.chatClient = chatClient;
        this.aiPromptsProperties = aiPromptsProperties;
        this.aiChatMemory = aiChatMemory;
    }

    @Override
    public ChatReply chat(ChatRequest chatRequest) {
        String inputId = chatRequest.sessionId();
        String finalSessionId = (inputId == null || inputId.isBlank()) ? UUID.randomUUID().toString() : inputId;
        String usertype = chatRequest.type();
        if (usertype == null || usertype.isBlank()||
        !aiPromptsProperties.prompt().containsKey(usertype)) {
            usertype = "default";
        }
        String Promptpath = aiPromptsProperties.prompt().get(usertype);
        ClassPathResource gpsPath =
                new ClassPathResource(Promptpath);
        String systememprompt = "";
        try {
            systememprompt =
                    new String(gpsPath.getInputStream().readAllBytes(),"UTF-8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ChatResponse answer = chatClient.prompt()
                .system(systememprompt)
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
