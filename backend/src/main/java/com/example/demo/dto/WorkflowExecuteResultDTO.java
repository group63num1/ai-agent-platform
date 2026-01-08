package com.example.demo.dto;

import lombok.Data;

import java.util.List;

@Data
public class WorkflowExecuteResultDTO {
    private Long workflowId;
    private String sessionId;
    private List<WorkflowNodeResultDTO> nodeResults;
    private String output;
}
