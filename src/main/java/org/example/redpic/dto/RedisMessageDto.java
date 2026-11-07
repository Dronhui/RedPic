package org.example.redpic.dto;

import java.time.LocalDateTime;

import lombok.Data;
@Data
public class RedisMessageDto {
    private String role;
    private String content;
    public  RedisMessageDto() {}
    public  RedisMessageDto(String role, String content) {
        this.role = role;
        this.content = content;
    }
}
