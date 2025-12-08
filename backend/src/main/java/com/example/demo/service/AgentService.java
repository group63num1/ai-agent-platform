package com.example.demo.service;

import com.example.demo.dto.AgentChatRequest;
import com.example.demo.dto.AgentDTO;
import com.example.demo.dto.AgentListResponse;
import com.example.demo.dto.AgentMessageDTO;
import com.example.demo.dto.AgentSessionCreateRequest;
import com.example.demo.dto.AgentSessionDTO;
import com.example.demo.dto.AgentUpdateRequest;
import com.example.demo.dto.AgentCreateRequest;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

public interface AgentService {
    AgentDTO createAgent(AgentCreateRequest request);

    AgentListResponse listAgents(int page, int pageSize, String keyword);

    List<AgentDTO> listPublishedAgents();

    AgentDTO getAgent(String agentId);

    AgentDTO updateAgent(String agentId, AgentUpdateRequest request);

    void deleteAgent(String agentId);

    AgentDTO publishAgent(String agentId);

    AgentDTO unpublishAgent(String agentId);

    AgentSessionDTO createSession(String agentId, AgentSessionCreateRequest request);

    void deleteSession(String agentId, String sessionId);

    List<AgentSessionDTO> listSessions(String agentId);

    SseEmitter chatWithAgent(String agentId, String sessionId, AgentChatRequest request);

    List<AgentMessageDTO> listMessages(String agentId, String sessionId, Integer limit);

    AgentSessionDTO updateSession(String agentId, String sessionId, com.example.demo.dto.AgentSessionUpdateRequest request);
}


