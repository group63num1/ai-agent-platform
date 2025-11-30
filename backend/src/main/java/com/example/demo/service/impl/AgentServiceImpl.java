package com.example.demo.service.impl;

import com.example.demo.app.entity.Agent;
import com.example.demo.app.entity.AgentMessage;
import com.example.demo.app.mapper.AgentMapper;
import com.example.demo.app.mapper.AgentMessageMapper;
import com.example.demo.dto.*;
import com.example.demo.service.AgentService;
import com.example.demo.service.LlmService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AgentServiceImpl implements AgentService {

    private static final String STATUS_DRAFT = "draft";
    private static final String STATUS_PUBLISHED = "published";
    private static final String DEFAULT_MODEL = "gpt-4o-mini";
    private static final int DEFAULT_CONTEXT_ROUNDS = 3;
    private static final int DEFAULT_MAX_TOKENS = 512;
    private static final String DEFAULT_SYSTEM_PROMPT =
            "You are an AI agent assistant. Answer accurately, concisely and stay within the given role.";

    @Autowired
    private AgentMapper agentMapper;

    @Autowired
    private AgentMessageMapper agentMessageMapper;

    @Autowired
    private LlmService llmService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public AgentDTO createAgent(AgentCreateRequest request) {
        validateAgentName(request.getName());
        ensureNameUnique(request.getName(), null);

        Agent agent = new Agent();
        agent.setId(generateAgentId());
        agent.setName(request.getName().trim());
        agent.setDescription(trimToNull(request.getDescription()));
        agent.setModel(isBlank(request.getModel()) ? DEFAULT_MODEL : request.getModel().trim());
        agent.setPrompt(trimToNull(request.getPrompt()));
        agent.setProfileMd(trimToNull(request.getProfileMd()));
        agent.setContextRounds(resolveContextRounds(request.getContextRounds()));
        agent.setMaxTokens(resolveMaxTokens(request.getMaxTokens()));
        agent.setPlugins(serializePlugins(request.getPlugins()));
        agent.setStatus(STATUS_DRAFT);
        agent.setSessionId(generateSessionId());
        LocalDateTime now = LocalDateTime.now();
        agent.setCreatedAt(now);
        agent.setUpdatedAt(now);

        if (agentMapper.insert(agent) <= 0) {
            throw new RuntimeException("创建智能体失败");
        }
        return toDTO(agent);
    }

    @Override
    public AgentListResponse listAgents(int page, int pageSize, String keyword) {
        int safePage = page <= 0 ? 1 : page;
        int safeSize = pageSize <= 0 ? 20 : Math.min(pageSize, 100);
        int offset = (safePage - 1) * safeSize;
        String normalizedKeyword = normalizeKeyword(keyword);

        List<Agent> agents = agentMapper.selectPage(normalizedKeyword, offset, safeSize);
        long total = agentMapper.countByKeyword(normalizedKeyword);

        AgentListResponse response = new AgentListResponse();
        response.setItems(agents.stream().map(this::toDTO).collect(Collectors.toList()));
        response.setTotal(total);
        response.setPage(safePage);
        response.setPageSize(safeSize);
        return response;
    }

    @Override
    public AgentDTO getAgent(String agentId) {
        Agent agent = requireAgent(agentId);
        return toDTO(agent);
    }

    @Override
    @Transactional
    public AgentDTO updateAgent(String agentId, AgentUpdateRequest request) {
        Agent agent = requireAgent(agentId);

        if (!isBlank(request.getName()) && !request.getName().trim().equals(agent.getName())) {
            validateAgentName(request.getName());
            ensureNameUnique(request.getName(), agentId);
            agent.setName(request.getName().trim());
        }

        if (request.getDescription() != null) {
            agent.setDescription(trimToNull(request.getDescription()));
        }
        if (!isBlank(request.getModel())) {
            agent.setModel(request.getModel().trim());
        }
        if (request.getPrompt() != null) {
            agent.setPrompt(trimToNull(request.getPrompt()));
        }
        if (request.getProfileMd() != null) {
            agent.setProfileMd(trimToNull(request.getProfileMd()));
        }
        if (request.getContextRounds() != null) {
            agent.setContextRounds(resolveContextRounds(request.getContextRounds()));
        }
        if (request.getMaxTokens() != null) {
            agent.setMaxTokens(resolveMaxTokens(request.getMaxTokens()));
        }
        if (request.getPlugins() != null) {
            agent.setPlugins(serializePlugins(request.getPlugins()));
        }
        if (!isBlank(request.getStatus())) {
            agent.setStatus(request.getStatus().trim());
        }
        if (!isBlank(request.getSessionId())) {
            agent.setSessionId(request.getSessionId().trim());
        }
        agent.setUpdatedAt(LocalDateTime.now());

        if (agentMapper.update(agent) <= 0) {
            throw new RuntimeException("更新智能体失败");
        }
        return toDTO(agent);
    }

    @Override
    @Transactional
    public void deleteAgent(String agentId) {
        Agent agent = requireAgent(agentId);
        if (!STATUS_DRAFT.equals(agent.getStatus())) {
            throw new RuntimeException("仅允许删除草稿状态的智能体");
        }
        if (agentMapper.delete(agentId) <= 0) {
            throw new RuntimeException("删除智能体失败");
        }
    }

    @Override
    @Transactional
    public AgentDTO publishAgent(String agentId) {
        Agent agent = requireAgent(agentId);
        if (STATUS_PUBLISHED.equals(agent.getStatus())) {
            throw new RuntimeException("智能体已发布");
        }
        if (isBlank(agent.getModel())) {
            throw new RuntimeException("发布智能体前需配置模型");
        }
        agent.setStatus(STATUS_PUBLISHED);
        agent.setUpdatedAt(LocalDateTime.now());
        if (agentMapper.update(agent) <= 0) {
            throw new RuntimeException("发布智能体失败");
        }
        return toDTO(agent);
    }

    @Override
    @Transactional
    public AgentChatResponse chatWithAgent(String agentId, AgentChatRequest request) {
        Agent agent = requireAgent(agentId);
        if (request == null || isBlank(request.getMessage())) {
            throw new RuntimeException("message不能为空");
        }

        String sessionId = isBlank(agent.getSessionId()) ? generateSessionId() : agent.getSessionId();
        if (!sessionId.equals(agent.getSessionId())) {
            agent.setSessionId(sessionId);
            agent.setUpdatedAt(LocalDateTime.now());
            agentMapper.update(agent);
        }

        int rounds = resolveContextRounds(
                request.getContextRounds() != null ? request.getContextRounds() : agent.getContextRounds());
        int historyLimit = rounds * 2;
        List<AgentMessage> history = historyLimit > 0
                ? agentMessageMapper.selectRecentMessages(agentId, sessionId, historyLimit)
                : Collections.emptyList();
        Collections.reverse(history);

        AgentMessage userMessage = new AgentMessage();
        userMessage.setAgentId(agentId);
        userMessage.setSessionId(sessionId);
        userMessage.setRole("user");
        userMessage.setContent(request.getMessage());
        userMessage.setCreatedAt(LocalDateTime.now());
        agentMessageMapper.insert(userMessage);

        List<Map<String, String>> llmMessages = buildLlmMessages(agent, history, userMessage);
        String aiResponse = llmService.generateResponse(llmMessages, agent.getModel());

        AgentMessage assistantMessage = new AgentMessage();
        assistantMessage.setAgentId(agentId);
        assistantMessage.setSessionId(sessionId);
        assistantMessage.setRole("assistant");
        assistantMessage.setContent(aiResponse);
        assistantMessage.setCreatedAt(LocalDateTime.now());
        agentMessageMapper.insert(assistantMessage);

        AgentChatResponse response = new AgentChatResponse();
        response.setRole("assistant");
        response.setContent(aiResponse);
        return response;
    }

    @Override
    public List<AgentMessageDTO> listMessages(String agentId, Integer limit) {
        Agent agent = requireAgent(agentId);
        int safeLimit = (limit == null || limit <= 0) ? 100 : Math.min(limit, 200);
        List<AgentMessage> messages = agentMessageMapper.selectLatestMessages(agent.getId(), safeLimit);
        Collections.reverse(messages);
        return messages.stream().map(this::toMessageDTO).collect(Collectors.toList());
    }

    private Agent requireAgent(String agentId) {
        if (isBlank(agentId)) {
            throw new RuntimeException("agentId不能为空");
        }
        Agent agent = agentMapper.selectById(agentId);
        if (agent == null) {
            throw new RuntimeException("智能体不存在");
        }
        return agent;
    }

    private void validateAgentName(String name) {
        if (isBlank(name)) {
            throw new RuntimeException("智能体名称不能为空");
        }
        String trimmed = name.trim();
        if (trimmed.length() > 64) {
            throw new RuntimeException("智能体名称长度不能超过64个字符");
        }
    }

    private void ensureNameUnique(String name, String excludeId) {
        Agent existing = agentMapper.selectByName(name.trim());
        if (existing != null && (excludeId == null || !existing.getId().equals(excludeId))) {
            throw new RuntimeException("智能体名称已存在");
        }
    }

    private int resolveContextRounds(Integer rounds) {
        if (rounds == null) {
            return DEFAULT_CONTEXT_ROUNDS;
        }
        if (rounds < 0) {
            return 0;
        }
        return Math.min(rounds, 20);
    }

    private int resolveMaxTokens(Integer maxTokens) {
        if (maxTokens == null) {
            return DEFAULT_MAX_TOKENS;
        }
        if (maxTokens < 64) {
            return 64;
        }
        return Math.min(maxTokens, 4096);
    }

    private String serializePlugins(List<String> plugins) {
        if (plugins == null) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(plugins);
        } catch (Exception e) {
            throw new RuntimeException("插件配置序列化失败", e);
        }
    }

    private List<String> deserializePlugins(String json) {
        if (isBlank(json)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private AgentDTO toDTO(Agent agent) {
        AgentDTO dto = new AgentDTO();
        dto.setId(agent.getId());
        dto.setName(agent.getName());
        dto.setDescription(agent.getDescription());
        dto.setModel(agent.getModel());
        dto.setPrompt(agent.getPrompt());
        dto.setProfileMd(agent.getProfileMd());
        dto.setContextRounds(agent.getContextRounds());
        dto.setMaxTokens(agent.getMaxTokens());
        dto.setPlugins(deserializePlugins(agent.getPlugins()));
        dto.setStatus(agent.getStatus());
        dto.setSessionId(agent.getSessionId());
        dto.setCreatedAt(agent.getCreatedAt());
        dto.setUpdatedAt(agent.getUpdatedAt());
        return dto;
    }

    private AgentMessageDTO toMessageDTO(AgentMessage message) {
        AgentMessageDTO dto = new AgentMessageDTO();
        dto.setId(message.getId());
        dto.setAgentId(message.getAgentId());
        dto.setSessionId(message.getSessionId());
        dto.setRole(message.getRole());
        dto.setContent(message.getContent());
        dto.setCreatedAt(message.getCreatedAt());
        return dto;
    }

    private List<Map<String, String>> buildLlmMessages(Agent agent,
                                                       List<AgentMessage> history,
                                                       AgentMessage latestUserMessage) {
        List<Map<String, String>> messages = new ArrayList<>();
        String systemPrompt = firstNonBlank(agent.getProfileMd(), agent.getPrompt(), DEFAULT_SYSTEM_PROMPT);
        messages.add(messageOf("system", systemPrompt));
        for (AgentMessage message : history) {
            messages.add(messageOf(message.getRole(), message.getContent()));
        }
        messages.add(messageOf(latestUserMessage.getRole(), latestUserMessage.getContent()));
        return messages;
    }

    private Map<String, String> messageOf(String role, String content) {
        Map<String, String> map = new HashMap<>();
        map.put("role", role);
        map.put("content", content);
        return map;
    }

    private String generateAgentId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private String generateSessionId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private String trimToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private String normalizeKeyword(String keyword) {
        return isBlank(keyword) ? null : keyword.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (!isBlank(value)) {
                return value.trim();
            }
        }
        return null;
    }
}


