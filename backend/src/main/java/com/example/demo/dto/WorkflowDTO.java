package com.example.demo.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class WorkflowDTO {
    private Long id;
    private String name;
    private String intro;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<String> agentIds;
}
