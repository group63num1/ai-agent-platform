// src/main/java/com/example/demo/app/entity/ChatMessage.java
package com.example.demo.app.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChatMessage {
    private Long id;
    private Long sessionId;
    private String role; // user/assistant/system
    private String content;
    private LocalDateTime createdAt;
}

