package com.example.demo.dto;

import lombok.Data;

import java.util.List;

@Data
public class WorkflowExecuteRequest {
    /**
     * 输入节点：用户输入（仅用于第一个agent的message）
     */
    private String input;

    /**
     * 每个节点的用户输入（index与agentIds顺序对齐；第0项不会参与第一个agent的message）
     */
    private List<String> nodeInputs;

    /**
     * 可选：前端自定义session_id；不传则后端生成
     */
    private String sessionId;
}
