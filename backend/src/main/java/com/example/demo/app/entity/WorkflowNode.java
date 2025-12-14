package com.example.demo.app.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WorkflowNode {
    private Long id;
    private Long workflowId;
    private Integer seq;
    private String agentId;
    private LocalDateTime createdAt;
}
