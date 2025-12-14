package com.example.demo.service;

import com.example.demo.dto.*;

import java.util.List;

public interface WorkflowService {
    List<WorkflowDTO> listMyWorkflows(Long userId);

    WorkflowDTO createWorkflow(Long userId, WorkflowCreateRequest request);

    WorkflowDTO saveWorkflow(Long userId, Long workflowId, WorkflowSaveRequest request);

    WorkflowDTO getWorkflowDetail(Long userId, Long workflowId);

    void deleteWorkflow(Long userId, Long workflowId);

    WorkflowExecuteResultDTO executeWorkflow(Long userId, Long workflowId, WorkflowExecuteRequest request);
}
