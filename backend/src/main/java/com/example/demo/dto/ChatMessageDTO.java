// src/main/java/com/example/demo/dto/ChatMessageDTO.java
package com.example.demo.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChatMessageDTO {
    private Long id;
    private Long sessionId;
    private String role; // user/assistant/system
    private String content;
    private LocalDateTime createdAt;
}

