package org.example.redpic.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiModelConfig {
    @Bean("memoryChatClient")
    public ChatClient memoryChatClient(@Qualifier("deepSeekChatModel")ChatModel chatModel
    , MessageWindowChatMemory aiChatMemory) {
        return ChatClient.builder(chatModel)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(aiChatMemory).build())
                .build();
    }

    @Bean("rewriteChatClient")
    public ChatClient rewriteChatClient(@Qualifier("deepSeekChatModel") ChatModel chatModel) {
        return ChatClient.builder(chatModel)
                .build();
    }

    @Bean("visionChatClient")
    public ChatClient visionChatClient(@Qualifier("openAiChatModel")ChatModel chatModel
    ){
        return ChatClient.builder(chatModel)
                .build();
    }


}
