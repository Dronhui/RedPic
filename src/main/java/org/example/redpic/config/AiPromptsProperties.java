package org.example.redpic.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "redpic.ai")
public record AiPromptsProperties(
        Map<String,String> prompt
){
}
