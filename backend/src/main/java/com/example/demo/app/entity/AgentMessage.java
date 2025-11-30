package com.example.demo.app.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AgentMessage {
    private Long id;
    private String agentId;
    private String sessionId;
    private String role;
    private String content;
    private LocalDateTime createdAt;
}


