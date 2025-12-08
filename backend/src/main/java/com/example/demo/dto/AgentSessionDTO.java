package com.example.demo.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AgentSessionDTO {
    private String sessionId;
    private String agentId;
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

