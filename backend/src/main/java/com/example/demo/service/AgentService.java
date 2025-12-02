package com.example.demo.service;

import com.example.demo.dto.*;

import java.util.List;

public interface AgentService {
    AgentDTO createAgent(AgentCreateRequest request);

    AgentListResponse listAgents(int page, int pageSize, String keyword);

    AgentDTO getAgent(String agentId);

    AgentDTO updateAgent(String agentId, AgentUpdateRequest request);

    void deleteAgent(String agentId);

    AgentDTO publishAgent(String agentId);

    AgentChatResponse chatWithAgent(String agentId, AgentChatRequest request);

    List<AgentMessageDTO> listMessages(String agentId, Integer limit);
}


