// src/main/java/com/example/demo/dto/ChatSessionDTO.java
package com.example.demo.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChatSessionDTO {
    private Long id;
    private Long userId;
    private String title;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer messageCount; // 消息数量
}

