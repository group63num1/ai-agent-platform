package com.example.demo.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AgentMessageDTO {
    private Long id;
    private String agentId;
    private String sessionId;
    private String role;
    private String content;
    private LocalDateTime createdAt;
}


