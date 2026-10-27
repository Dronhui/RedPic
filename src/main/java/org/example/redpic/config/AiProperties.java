package org.example.redpic.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "redpic.ai")
public record AiProperties (
        String systemPrompt
){
}
