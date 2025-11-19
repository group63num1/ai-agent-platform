// src/main/java/com/example/demo/dto/ChatResponseDTO.java
package com.example.demo.dto;

import lombok.Data;

@Data
public class ChatResponseDTO {
    private ChatMessageDTO userMessage; // 用户消息
    private ChatMessageDTO assistantMessage; // AI助手回复
}

