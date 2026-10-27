package org.example.redpic.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "redpic.ai")
public record AiProperties (
        String systemPrompt
){
}
