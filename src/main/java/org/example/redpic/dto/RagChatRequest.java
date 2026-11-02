package org.example.redpic.dto;

import jakarta.validation.constraints.NotBlank;

public record RagChatRequest(
        @NotBlank(message="内容不能为空")
        String question
        ) {
}
