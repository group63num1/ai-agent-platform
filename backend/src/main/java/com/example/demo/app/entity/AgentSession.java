package com.example.demo.app.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AgentSession {
    private String sessionId;
    private String agentId;
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

