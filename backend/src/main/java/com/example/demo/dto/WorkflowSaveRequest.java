package com.example.demo.dto;

import lombok.Data;

import java.util.List;

@Data
public class WorkflowSaveRequest {
    /**
     * 按调用顺序排列的已发布agentId列表
     */
    private List<String> agentIds;
}
