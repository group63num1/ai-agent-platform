package com.example.demo.service.impl;

import com.example.demo.app.entity.Agent;
import com.example.demo.app.entity.Workflow;
import com.example.demo.app.entity.WorkflowNode;
import com.example.demo.app.mapper.AgentMapper;
import com.example.demo.app.mapper.WorkflowMapper;
import com.example.demo.app.mapper.WorkflowNodeMapper;
import com.example.demo.dto.*;
import com.example.demo.service.AiAgentClient;
import com.example.demo.service.WorkflowService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WorkflowServiceImpl implements WorkflowService {

    private static final String AGENT_STATUS_PUBLISHED = "published";

    @Autowired
    private WorkflowMapper workflowMapper;

    @Autowired
    private WorkflowNodeMapper workflowNodeMapper;

    @Autowired
    private AgentMapper agentMapper;

    @Autowired
    private AiAgentClient aiAgentClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<WorkflowDTO> listMyWorkflows(Long userId) {
        requireUserId(userId);
        List<Workflow> workflows = workflowMapper.selectByUserId(userId);
        if (workflows == null || workflows.isEmpty()) {
            return Collections.emptyList();
        }
        return workflows.stream().map(w -> {
            WorkflowDTO dto = new WorkflowDTO();
            dto.setId(w.getId());
            dto.setName(w.getName());
            dto.setIntro(w.getIntro());
            dto.setCreatedAt(w.getCreatedAt());
            dto.setUpdatedAt(w.getUpdatedAt());
            dto.setAgentIds(Collections.emptyList());
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public WorkflowDTO createWorkflow(Long userId, WorkflowCreateRequest request) {
        requireUserId(userId);
        if (request == null || isBlank(request.getName())) {
            throw new RuntimeException("工作流名称不能为空");
        }

        Workflow wf = new Workflow();
        wf.setUserId(userId);
        wf.setName(request.getName().trim());
        wf.setIntro(trimToNull(request.getIntro()));
        LocalDateTime now = LocalDateTime.now();
        wf.setCreatedAt(now);
        wf.setUpdatedAt(now);

        if (workflowMapper.insert(wf) <= 0) {
            throw new RuntimeException("创建工作流失败");
        }

        WorkflowDTO dto = new WorkflowDTO();
        dto.setId(wf.getId());
        dto.setName(wf.getName());
        dto.setIntro(wf.getIntro());
        dto.setCreatedAt(wf.getCreatedAt());
        dto.setUpdatedAt(wf.getUpdatedAt());
        dto.setAgentIds(Collections.emptyList());
        return dto;
    }

    @Override
    @Transactional
    public WorkflowDTO saveWorkflow(Long userId, Long workflowId, WorkflowSaveRequest request) {
        requireUserId(userId);
        Workflow wf = requireOwnedWorkflow(userId, workflowId);
        List<String> agentIds = request == null ? null : request.getAgentIds();
        if (agentIds == null) {
            agentIds = Collections.emptyList();
        }

        // 校验每个agent必须已发布
        for (String agentId : agentIds) {
            if (isBlank(agentId)) {
                throw new RuntimeException("agentId不能为空");
            }
            Agent agent = agentMapper.selectById(agentId.trim());
            if (agent == null) {
                throw new RuntimeException("agent不存在: " + agentId);
            }
            if (!AGENT_STATUS_PUBLISHED.equalsIgnoreCase(trimToEmpty(agent.getStatus()))) {
                throw new RuntimeException("agent未发布: " + agentId);
            }
        }

        workflowNodeMapper.deleteByWorkflowId(wf.getId());
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < agentIds.size(); i++) {
            WorkflowNode node = new WorkflowNode();
            node.setWorkflowId(wf.getId());
            node.setSeq(i);
            node.setAgentId(agentIds.get(i).trim());
            node.setCreatedAt(now);
            workflowNodeMapper.insert(node);
        }

        wf.setUpdatedAt(now);
        workflowMapper.update(wf);
        return getWorkflowDetail(userId, wf.getId());
    }

    @Override
    public WorkflowDTO getWorkflowDetail(Long userId, Long workflowId) {
        requireUserId(userId);
        Workflow wf = requireOwnedWorkflow(userId, workflowId);
        List<WorkflowNode> nodes = workflowNodeMapper.selectByWorkflowId(wf.getId());

        WorkflowDTO dto = new WorkflowDTO();
        dto.setId(wf.getId());
        dto.setName(wf.getName());
        dto.setIntro(wf.getIntro());
        dto.setCreatedAt(wf.getCreatedAt());
        dto.setUpdatedAt(wf.getUpdatedAt());
        dto.setAgentIds(nodes == null ? Collections.emptyList() : nodes.stream().map(WorkflowNode::getAgentId).collect(Collectors.toList()));
        return dto;
    }

    @Override
    @Transactional
    public void deleteWorkflow(Long userId, Long workflowId) {
        requireUserId(userId);
        Workflow wf = requireOwnedWorkflow(userId, workflowId);
        workflowMapper.deleteById(wf.getId());
        // workflow_nodes 由外键级联删除（或这里deleteByWorkflowId兜底）
        workflowNodeMapper.deleteByWorkflowId(wf.getId());
    }

    @Override
    public WorkflowExecuteResultDTO executeWorkflow(Long userId, Long workflowId, WorkflowExecuteRequest request) {
        requireUserId(userId);
        Workflow wf = requireOwnedWorkflow(userId, workflowId);
        List<WorkflowNode> nodes = workflowNodeMapper.selectByWorkflowId(wf.getId());
        if (nodes == null || nodes.isEmpty()) {
            throw new RuntimeException("工作流节点为空");
        }
        if (request == null || isBlank(request.getInput())) {
            throw new RuntimeException("input不能为空");
        }

        String sessionId = isBlank(request.getSessionId()) ? UUID.randomUUID().toString().replace("-", "") : request.getSessionId().trim();
        List<String> nodeInputs = request.getNodeInputs() == null ? Collections.emptyList() : request.getNodeInputs();

        List<WorkflowNodeResultDTO> results = new ArrayList<>();
        String previousOutput = null;

        for (int i = 0; i < nodes.size(); i++) {
            WorkflowNode node = nodes.get(i);
            Agent agent = agentMapper.selectById(node.getAgentId());
            if (agent == null) {
                throw new RuntimeException("agent不存在: " + node.getAgentId());
            }
            if (!AGENT_STATUS_PUBLISHED.equalsIgnoreCase(trimToEmpty(agent.getStatus()))) {
                throw new RuntimeException("agent未发布: " + node.getAgentId());
            }

            String message;
            if (i == 0) {
                message = request.getInput().trim();
            } else {
                String userInputForNode = i < nodeInputs.size() ? nodeInputs.get(i) : null;
                message = buildMessage(previousOutput, userInputForNode);
            }

            Map<String, Object> payload = new HashMap<>();
            payload.put("message", message);
            payload.put("model_id", isBlank(agent.getModel()) ? "" : agent.getModel().trim());
            payload.put("session_id", sessionId);
            payload.put("knowledge_bases", parseJsonStringArray(agent.getKnowledgeBase()));
            payload.put("tools", parseJsonStringArray(agent.getPlugins()));
            payload.put("history", Collections.emptyList());

            String output;
            try {
                output = aiAgentClient.chatOnce(payload);
            } catch (Exception e) {
                throw new RuntimeException("执行节点失败: agentId=" + node.getAgentId() + ", err=" + e.getMessage(), e);
            }

            WorkflowNodeResultDTO nodeResult = new WorkflowNodeResultDTO();
            nodeResult.setAgentId(node.getAgentId());
            nodeResult.setOutput(output);
            results.add(nodeResult);

            previousOutput = output;
        }

        WorkflowExecuteResultDTO resp = new WorkflowExecuteResultDTO();
        resp.setWorkflowId(wf.getId());
        resp.setSessionId(sessionId);
        resp.setNodeResults(results);
        resp.setOutput(previousOutput);
        return resp;
    }

    private Workflow requireOwnedWorkflow(Long userId, Long workflowId) {
        if (workflowId == null) {
            throw new RuntimeException("workflowId不能为空");
        }
        Workflow wf = workflowMapper.selectById(workflowId);
        if (wf == null) {
            throw new RuntimeException("工作流不存在");
        }
        if (wf.getUserId() == null || !wf.getUserId().equals(userId)) {
            throw new RuntimeException("无权限访问该工作流");
        }
        return wf;
    }

    private void requireUserId(Long userId) {
        if (userId == null) {
            throw new RuntimeException("未登录或token无效");
        }
    }

    private List<String> parseJsonStringArray(String json) {
        if (isBlank(json)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private String buildMessage(String previousOutput, String userInput) {
        String prev = trimToNull(previousOutput);
        String ui = trimToNull(userInput);
        if (prev == null && ui == null) {
            return "";
        }
        if (prev == null) {
            return ui;
        }
        if (ui == null) {
            return prev;
        }
        return prev + "\n\n" + ui;
    }

    private boolean isBlank(String v) {
        return v == null || v.trim().isEmpty();
    }

    private String trimToNull(String v) {
        return isBlank(v) ? null : v.trim();
    }

    private String trimToEmpty(String v) {
        return v == null ? "" : v.trim();
    }
}
