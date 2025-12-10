package com.example.demo.service.impl;

import com.example.demo.app.entity.Agent;
import com.example.demo.app.entity.AgentMessage;
import com.example.demo.app.entity.AgentSession;
import com.example.demo.app.mapper.AgentMapper;
import com.example.demo.app.mapper.AgentMessageMapper;
import com.example.demo.app.mapper.AgentSessionMapper;
import com.example.demo.dto.*;
import com.example.demo.service.AiAgentClient;
import com.example.demo.service.AgentService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class AgentServiceImpl implements AgentService {

    private static final String STATUS_DRAFT = "draft";
    private static final String STATUS_PUBLISHED = "published";
    // 使用数据库中存在的模型作为默认值
    private static final String DEFAULT_MODEL = "qwen-max";
    // 数据库中可用的模型列表
    private static final Set<String> VALID_MODELS = Set.of("qwen-max", "qwen-mt-plus", "qwen3-max");
    private static final int DEFAULT_CONTEXT_ROUNDS = 3;
    private static final int DEFAULT_MAX_TOKENS = 512;
    private static final String DEFAULT_SYSTEM_PROMPT =
            "You are an AI agent assistant. Answer accurately, concisely and stay within the given role.";
    private static final String STREAM_DONE_TOKEN = "[DONE]";

    @Autowired
    private AgentMapper agentMapper;

    @Autowired
    private AgentMessageMapper agentMessageMapper;

    @Autowired
    private AgentSessionMapper agentSessionMapper;

    @Autowired
    private AiAgentClient aiAgentClient;

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
        String model = isBlank(request.getModel()) ? DEFAULT_MODEL : request.getModel().trim();
        // 验证模型是否存在，如果不存在则使用默认模型
        if (!VALID_MODELS.contains(model)) {
            System.out.println("警告: 模型 '" + model + "' 不存在，使用默认模型 '" + DEFAULT_MODEL + "'");
            model = DEFAULT_MODEL;
        }
        agent.setModel(model);
        agent.setPrompt(trimToNull(request.getPrompt()));
        agent.setProfileMd(trimToNull(request.getProfileMd()));
        agent.setContextRounds(resolveContextRounds(request.getContextRounds()));
        agent.setMaxTokens(resolveMaxTokens(request.getMaxTokens()));
        agent.setPlugins(serializePlugins(request.getPlugins()));
        agent.setKnowledgeBase(serializeKnowledgeBase(request.getKnowledgeBase()));
        agent.setStatus(STATUS_DRAFT);
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
    public List<AgentDTO> listPublishedAgents() {
        List<Agent> publishedAgents = agentMapper.selectByStatus(STATUS_PUBLISHED);
        return publishedAgents.stream().map(this::toDTO).collect(Collectors.toList());
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
            String model = request.getModel().trim();
            // 验证模型是否存在，如果不存在则使用默认模型
            if (!VALID_MODELS.contains(model)) {
                System.out.println("警告: 模型 '" + model + "' 不存在，使用默认模型 '" + DEFAULT_MODEL + "'");
                model = DEFAULT_MODEL;
            }
            agent.setModel(model);
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
        if (request.getKnowledgeBase() != null) {
            agent.setKnowledgeBase(serializeKnowledgeBase(request.getKnowledgeBase()));
        }
        if (!isBlank(request.getStatus())) {
            agent.setStatus(request.getStatus().trim());
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
    public AgentDTO unpublishAgent(String agentId) {
        Agent agent = requireAgent(agentId);
        if (!STATUS_PUBLISHED.equals(agent.getStatus())) {
            throw new RuntimeException("仅已发布的智能体可下架");
        }
        agent.setStatus(STATUS_DRAFT);
        agent.setUpdatedAt(LocalDateTime.now());
        if (agentMapper.update(agent) <= 0) {
            throw new RuntimeException("下架智能体失败");
        }
        return toDTO(agent);
    }

    @Override
    @Transactional
    public AgentSessionDTO createSession(String agentId, AgentSessionCreateRequest request) {
        Agent agent = requireAgent(agentId);
        AgentSession session = new AgentSession();
        session.setSessionId(generateSessionId());
        session.setAgentId(agent.getId());
        session.setName(request == null ? null : trimToNull(request.getName()));
        LocalDateTime now = LocalDateTime.now();
        session.setCreatedAt(now);
        session.setUpdatedAt(now);
        if (agentSessionMapper.insert(session) <= 0) {
            throw new RuntimeException("创建会话失败");
        }
        return toSessionDTO(session);
    }

    @Override
    @Transactional
    public void deleteSession(String agentId, String sessionId) {
        requireAgent(agentId);
        AgentSession session = requireSession(sessionId, agentId);
        if (agentSessionMapper.deleteById(session.getSessionId()) <= 0) {
            throw new RuntimeException("删除会话失败");
        }
    }

    @Override
    public List<AgentSessionDTO> listSessions(String agentId) {
        requireAgent(agentId);
        List<AgentSession> sessions = agentSessionMapper.selectByAgentId(agentId);
        return sessions.stream().map(this::toSessionDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AgentSessionDTO updateSession(String agentId, String sessionId, AgentSessionUpdateRequest request) {
        AgentSession session = requireSession(sessionId, agentId);
        if (request != null && !isBlank(request.getName())) {
            session.setName(request.getName().trim());
        }
        session.setUpdatedAt(LocalDateTime.now());
        if (agentSessionMapper.updateName(session.getSessionId(), session.getName(), session.getUpdatedAt()) <= 0) {
            throw new RuntimeException("更新会话名称失败");
        }
        return toSessionDTO(session);
    }

    @Override
    public SseEmitter chatWithAgent(String agentId, String sessionId, AgentChatRequest request) {
        Agent agent = requireAgent(agentId);
        if (isBlank(sessionId)) {
            throw new RuntimeException("sessionId不能为空");
        }
        AgentSession session = requireSession(sessionId, agentId);
        if (request == null || isBlank(request.getMessage())) {
            throw new RuntimeException("message不能为空");
        }

        int rounds = resolveContextRounds(agent.getContextRounds());
        int historyLimit = rounds * 2;
        List<AgentMessage> history = historyLimit > 0
                ? agentMessageMapper.selectRecentMessages(session.getSessionId(), historyLimit)
                : Collections.emptyList();
        Collections.reverse(history);

        AgentMessage userMessage = new AgentMessage();
        userMessage.setSessionId(session.getSessionId());
        userMessage.setRole("user");
        userMessage.setContent(request.getMessage());
        userMessage.setCreatedAt(LocalDateTime.now());
        agentMessageMapper.insert(userMessage);

        Map<String, Object> proxyPayload = buildAiAgentPayload(agent, session.getSessionId(), request, history);

        SseEmitter emitter = new SseEmitter(0L);
        CompletableFuture.runAsync(() -> forwardStream(proxyPayload, session.getSessionId(), emitter));
        return emitter;
    }

    @Override
    public List<AgentMessageDTO> listMessages(String agentId, String sessionId, Integer limit) {
        requireAgent(agentId);
        AgentSession session = requireSession(sessionId, agentId);
        int safeLimit = (limit == null || limit <= 0) ? 100 : Math.min(limit, 200);
        List<AgentMessage> messages = agentMessageMapper.selectLatestMessages(session.getSessionId(), safeLimit);
        Collections.reverse(messages);
        return messages.stream().map(this::toMessageDTO).collect(Collectors.toList());
    }

    private Map<String, Object> buildAiAgentPayload(Agent agent,
                                                   String sessionId,
                                                   AgentChatRequest request,
                                                   List<AgentMessage> history) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("message", request.getMessage());
        payload.put("model_id", resolveModelName(agent));
        payload.put("session_id", sessionId);

        String systemPrompt = firstNonBlank(agent.getProfileMd(), agent.getPrompt(), DEFAULT_SYSTEM_PROMPT);
        if (!isBlank(systemPrompt)) {
            payload.put("system_prompt", systemPrompt);
        }

        List<String> plugins = deserializePlugins(agent.getPlugins());
        List<String> knowledgeBases = deserializeKnowledgeBase(agent.getKnowledgeBase());
        payload.put("tools", plugins);
        payload.put("knowledge_bases", knowledgeBases);
        payload.put("history", buildHistoryPayload(history));
        return payload;
    }

    private String resolveModelName(Agent agent) {
        String model = agent.getModel();
        if (!isBlank(model)) {
            // 验证模型是否存在于数据库中，如果不存在则使用默认模型
            String normalizedModel = model.trim();
            if (VALID_MODELS.contains(normalizedModel)) {
                return normalizedModel;
            } else {
                // 如果模型不存在，使用默认模型并记录警告
                System.out.println("警告: 模型 '" + normalizedModel + "' 不存在，使用默认模型 '" + DEFAULT_MODEL + "'");
                return DEFAULT_MODEL;
            }
        }
        return DEFAULT_MODEL;
    }

    private List<Map<String, String>> buildHistoryPayload(List<AgentMessage> history) {
        if (history == null || history.isEmpty()) {
            return Collections.emptyList();
        }
        return history.stream()
                .map(message -> {
                    Map<String, String> map = new HashMap<>();
                    map.put("role", message.getRole());
                    map.put("content", message.getContent());
                    return map;
                })
                .collect(Collectors.toList());
    }

    private void forwardStream(Map<String, Object> proxyPayload,
                               String sessionId,
                               SseEmitter emitter) {
        StringBuilder assistantBuilder = new StringBuilder();
        try {
            System.out.println("开始转发请求到 AI Agent");
            System.out.println("Payload: " + proxyPayload);
            aiAgentClient.streamChat(proxyPayload, data -> handleStreamChunk(data, emitter, assistantBuilder));
            System.out.println("AI Agent 响应完成，内容长度: " + assistantBuilder.length());
            saveAssistantMessage(sessionId, assistantBuilder.toString());
            emitter.complete();
        } catch (Exception ex) {
            System.err.println("转发请求到 AI Agent 失败: " + ex.getMessage());
            System.err.println("异常类型: " + ex.getClass().getName());
            if (ex instanceof java.net.UnknownHostException) {
                System.err.println("网络错误: 无法解析主机名，请检查 Docker 网络配置和 AI_AGENT_BASE_URL 环境变量");
            }
            ex.printStackTrace();
            handleStreamError(emitter, ex);
        }
    }

    private void handleStreamChunk(String data,
                                   SseEmitter emitter,
                                   StringBuilder assistantBuilder) {
        try {
            // 检查是否是错误响应
            if (data != null && data.trim().startsWith("{")) {
                try {
                    JsonNode node = objectMapper.readTree(data);
                    if (node.has("error") || (node.has("success") && !node.get("success").asBoolean())) {
                        System.err.println("收到 AI Agent 错误响应: " + data);
                        // 错误响应也要发送给前端
                        emitter.send(data, MediaType.TEXT_PLAIN);
                        return;
                    }
                } catch (Exception e) {
                    // 不是 JSON 格式，继续正常处理
                }
            }
            emitter.send(data, MediaType.TEXT_PLAIN);
        } catch (IOException e) {
            System.err.println("SSE发送失败: " + e.getMessage());
            throw new RuntimeException("SSE发送失败", e);
        }
        if (!isDoneEvent(data)) {
            appendAssistantContent(data, assistantBuilder);
        }
    }

    private boolean isDoneEvent(String data) {
        return data != null && STREAM_DONE_TOKEN.equalsIgnoreCase(data.trim());
    }

    private void appendAssistantContent(String data, StringBuilder buffer) {
        if (buffer == null || isDoneEvent(data) || isBlank(data)) {
            return;
        }
        try {
            JsonNode node = objectMapper.readTree(data);
            JsonNode contentNode = node.get("content");
            if (contentNode != null && !contentNode.isNull()) {
                String content = contentNode.asText();
                if (!isBlank(content)) {
                    buffer.append(content);
                }
            }
        } catch (Exception e) {
            // 如果不是标准 JSON 片段，尝试直接使用原始数据
            System.out.println("解析 JSON 失败，使用原始数据: " + data.substring(0, Math.min(100, data.length())));
            if (!data.trim().equals("[DONE]") && !data.trim().startsWith("{")) {
                buffer.append(data);
            }
        }
    }

    private void saveAssistantMessage(String sessionId, String content) {
        if (isBlank(content)) {
            return;
        }
        AgentMessage assistantMessage = new AgentMessage();
        assistantMessage.setSessionId(sessionId);
        assistantMessage.setRole("assistant");
        assistantMessage.setContent(content);
        assistantMessage.setCreatedAt(LocalDateTime.now());
        agentMessageMapper.insert(assistantMessage);
    }

    private void handleStreamError(SseEmitter emitter, Exception ex) {
        try {
            System.err.println("处理流式错误: " + ex.getMessage());
            ex.printStackTrace();
            
            // 构建错误信息
            String errorMessage = ex.getMessage();
            if (ex instanceof java.net.UnknownHostException) {
                errorMessage = "无法连接到 AI Agent 服务，请检查服务是否正常运行";
                System.err.println("网络连接错误: 无法解析主机名 'ai-agent'，请检查 Docker 网络配置");
            }
            
            Map<String, Object> errorPayload = new HashMap<>();
            errorPayload.put("success", false);
            errorPayload.put("error", errorMessage);
            errorPayload.put("content", ""); // 确保前端能识别这是错误
            
            try {
                String errorJson = objectMapper.writeValueAsString(errorPayload);
                emitter.send("data: " + errorJson + "\n\n", MediaType.TEXT_PLAIN);
            } catch (Exception sendEx) {
                System.err.println("发送错误信息失败: " + sendEx.getMessage());
                // 如果发送失败，尝试发送简单的错误信息
                try {
                    emitter.send("data: {\"success\":false,\"error\":\"" + errorMessage.replace("\"", "\\\"") + "\"}\n\n", MediaType.TEXT_PLAIN);
                } catch (Exception ignore) {
                    // 忽略
                }
            }
        } catch (Exception e) {
            System.err.println("处理流式错误时发生异常: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                emitter.complete();
            } catch (Exception ignore) {
                // 忽略完成时的异常
            }
        }
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

    private AgentSession requireSession(String sessionId, String agentId) {
        if (isBlank(sessionId)) {
            throw new RuntimeException("sessionId不能为空");
        }
        AgentSession session = agentSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new RuntimeException("会话不存在");
        }
        if (agentId != null && !session.getAgentId().equals(agentId)) {
            throw new RuntimeException("会话不属于当前智能体");
        }
        return session;
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

    private String serializeKnowledgeBase(List<String> knowledgeBase) {
        if (knowledgeBase == null) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(knowledgeBase);
        } catch (Exception e) {
            throw new RuntimeException("知识库配置序列化失败", e);
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

    private List<String> deserializeKnowledgeBase(String json) {
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
        dto.setKnowledgeBase(deserializeKnowledgeBase(agent.getKnowledgeBase()));
        dto.setStatus(agent.getStatus());
        dto.setCreatedAt(agent.getCreatedAt());
        dto.setUpdatedAt(agent.getUpdatedAt());
        return dto;
    }

    private AgentMessageDTO toMessageDTO(AgentMessage message) {
        AgentMessageDTO dto = new AgentMessageDTO();
        dto.setId(message.getId());
        dto.setSessionId(message.getSessionId());
        dto.setRole(message.getRole());
        dto.setContent(message.getContent());
        dto.setCreatedAt(message.getCreatedAt());
        return dto;
    }

    private AgentSessionDTO toSessionDTO(AgentSession session) {
        AgentSessionDTO dto = new AgentSessionDTO();
        dto.setSessionId(session.getSessionId());
        dto.setAgentId(session.getAgentId());
        dto.setName(session.getName());
        dto.setCreatedAt(session.getCreatedAt());
        dto.setUpdatedAt(session.getUpdatedAt());
        return dto;
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



