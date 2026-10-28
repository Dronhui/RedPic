package org.example.redpic.dto;
import jakarta.validation.constraints.NotBlank;
public record ChatRequest(
    @NotBlank(message = "请输入不能为空")
    String message,
    String sessionId,
    String type
){

}
