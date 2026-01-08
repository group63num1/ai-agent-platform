// src/main/java/com/example/demo/service/impl/ChatServiceImpl.java
package com.example.demo.service.impl;

import com.example.demo.app.entity.ChatSession;
import com.example.demo.app.entity.ChatMessage;
import com.example.demo.app.mapper.ChatSessionMapper;
import com.example.demo.app.mapper.ChatMessageMapper;
import com.example.demo.dto.*;
import com.example.demo.service.ChatService;
import com.example.demo.service.LlmService;
import com.example.demo.service.SecurityService;
import com.example.demo.util.BeanConvertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatServiceImpl implements ChatService {

    @Autowired
    private ChatSessionMapper chatSessionMapper;

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Autowired
    private LlmService llmService;
    
    @Autowired
    private com.example.demo.config.LlmConfig llmConfig;

    @Autowired
    private SecurityService securityService;

    @Override
    @Transactional
    public ChatSessionDTO createSession(Long userId, ChatSessionCreateDTO createDTO) {
        ChatSession session = new ChatSession();
        session.setUserId(userId);
        
        // 如果提供了标题，使用提供的标题；否则使用默认标题
        if (createDTO.getTitle() != null && !createDTO.getTitle().trim().isEmpty()) {
            session.setTitle(createDTO.getTitle().trim());
        } else {
            session.setTitle("新对话");
        }
        
        session.setIsDeleted(0);
        session.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());

        int result = chatSessionMapper.insert(session);
        if (result <= 0) {
            throw new RuntimeException("创建会话失败");
        }

        ChatSessionDTO sessionDTO = BeanConvertUtil.convert(session, ChatSessionDTO.class);
        sessionDTO.setMessageCount(0);
        return sessionDTO;
    }

    @Override
    public List<ChatSessionDTO> getUserSessions(Long userId) {
        List<ChatSession> sessions = chatSessionMapper.selectByUserId(userId, false);
        return sessions.stream().map(session -> {
            ChatSessionDTO dto = BeanConvertUtil.convert(session, ChatSessionDTO.class);
            // 获取消息数量
            int messageCount = chatMessageMapper.countBySessionId(session.getId());
            dto.setMessageCount(messageCount);
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public ChatSessionDTO getSessionById(Long sessionId, Long userId) {
        ChatSession session = chatSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new RuntimeException("会话不存在");
        }

        // 验证会话是否属于该用户
        if (!session.getUserId().equals(userId)) {
            throw new RuntimeException("无权访问该会话");
        }

        ChatSessionDTO sessionDTO = BeanConvertUtil.convert(session, ChatSessionDTO.class);
        int messageCount = chatMessageMapper.countBySessionId(sessionId);
        sessionDTO.setMessageCount(messageCount);
        return sessionDTO;
    }

    @Override
    @Transactional
    public void deleteSession(Long sessionId, Long userId) {
        ChatSession session = chatSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new RuntimeException("会话不存在");
        }

        // 验证会话是否属于该用户
        if (!session.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除该会话");
        }

        int result = chatSessionMapper.softDelete(sessionId);
        if (result <= 0) {
            throw new RuntimeException("删除会话失败");
        }
    }

    @Override
    @Transactional
    public ChatSessionDTO updateSessionTitle(Long sessionId, Long userId, String title) {
        ChatSession session = chatSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new RuntimeException("会话不存在");
        }

        // 验证会话是否属于该用户
        if (!session.getUserId().equals(userId)) {
            throw new RuntimeException("无权修改该会话");
        }

        int result = chatSessionMapper.updateTitle(sessionId, title.trim());
        if (result <= 0) {
            throw new RuntimeException("更新会话标题失败");
        }

        return getSessionById(sessionId, userId);
    }

    @Override
    @Transactional
    public ChatResponseDTO sendMessage(Long userId, ChatMessageCreateDTO messageDTO, 
                                      String ipAddress, String userAgent) {
        // 验证会话是否存在且属于该用户
        ChatSession session = chatSessionMapper.selectById(messageDTO.getSessionId());
        if (session == null) {
            throw new RuntimeException("会话不存在");
        }
        if (!session.getUserId().equals(userId)) {
            throw new RuntimeException("无权访问该会话");
        }

        // 1. 安全检查：敏感内容过滤、风险检测、隐私保护
        SecurityCheckResultDTO securityCheck = securityService.checkContent(
            messageDTO.getContent(), userId, messageDTO.getSessionId(), ipAddress, userAgent
        );

        // 2. 如果被阻止，抛出异常
        if (securityCheck.getBlocked()) {
            throw new RuntimeException(securityCheck.getWarningMessage() != null ? 
                securityCheck.getWarningMessage() : "消息包含敏感内容，已被阻止");
        }

        // 3. 使用过滤后的内容（如果被过滤）
        String filteredContent = securityCheck.getFilteredContent() != null ? 
            securityCheck.getFilteredContent() : messageDTO.getContent();

        // 4. 保存用户消息（使用过滤后的内容）
        ChatMessage userMessage = new ChatMessage();
        userMessage.setSessionId(messageDTO.getSessionId());
        userMessage.setRole("user");
        userMessage.setContent(filteredContent);
        userMessage.setCreatedAt(LocalDateTime.now());

        int result = chatMessageMapper.insert(userMessage);
        if (result <= 0) {
            throw new RuntimeException("保存用户消息失败");
        }

        // 5. 生成AI回复
        String aiResponse = generateAIResponse(messageDTO.getSessionId(), filteredContent);

        // 6. 检测AI回复中的隐私数据
        securityService.detectPrivacyData(aiResponse, userId, messageDTO.getSessionId(), "ai_response");

        // 7. 保存AI回复
        ChatMessage assistantMessage = new ChatMessage();
        assistantMessage.setSessionId(messageDTO.getSessionId());
        assistantMessage.setRole("assistant");
        assistantMessage.setContent(aiResponse);
        assistantMessage.setCreatedAt(LocalDateTime.now());

        result = chatMessageMapper.insert(assistantMessage);
        if (result <= 0) {
            throw new RuntimeException("保存AI回复失败");
        }

        // 8. 更新会话的更新时间
        ChatSession updateSession = new ChatSession();
        updateSession.setId(messageDTO.getSessionId());
        updateSession.setUpdatedAt(LocalDateTime.now());
        chatSessionMapper.update(updateSession);

        // 9. 构建响应
        ChatResponseDTO response = new ChatResponseDTO();
        response.setUserMessage(BeanConvertUtil.convert(userMessage, ChatMessageDTO.class));
        response.setAssistantMessage(BeanConvertUtil.convert(assistantMessage, ChatMessageDTO.class));

        // 10. 如果有警告消息，添加到响应中（可以通过扩展DTO来传递）
        // 这里暂时不修改DTO，如果需要可以在前端通过其他方式获取

        return response;
    }

    @Override
    public List<ChatMessageDTO> getSessionMessages(Long sessionId, Long userId, Long lastMessageId, Integer limit) {
        // 验证会话是否存在且属于该用户
        ChatSession session = chatSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new RuntimeException("会话不存在");
        }
        if (!session.getUserId().equals(userId)) {
            throw new RuntimeException("无权访问该会话");
        }

        // 设置默认分页大小
        if (limit == null || limit <= 0) {
            limit = 20;
        }
        if (limit > 100) {
            limit = 100; // 限制最大分页大小
        }

        List<ChatMessage> messages;
        if (lastMessageId != null && lastMessageId > 0) {
            messages = chatMessageMapper.selectBySessionIdWithPagination(sessionId, lastMessageId, limit);
        } else {
            // 获取所有消息（首次加载）
            messages = chatMessageMapper.selectBySessionId(sessionId);
        }

        return messages.stream()
                .map(msg -> BeanConvertUtil.convert(msg, ChatMessageDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public int getMessageCount(Long sessionId, Long userId) {
        // 验证会话是否存在且属于该用户
        ChatSession session = chatSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new RuntimeException("会话不存在");
        }
        if (!session.getUserId().equals(userId)) {
            throw new RuntimeException("无权访问该会话");
        }

        return chatMessageMapper.countBySessionId(sessionId);
    }

    /**
     * 生成AI回复
     * 获取会话历史消息，构建上下文，然后调用LLM API生成回复
     * 支持多轮对话上下文记忆
     */
    private String generateAIResponse(Long sessionId, String userMessage) {
        // 获取会话历史消息（用于构建上下文）
        // 注意：此时用户消息已经保存到数据库（sendMessage方法第142-151行），
        // 所以历史消息查询结果中已经包含了刚保存的用户消息
        List<ChatMessage> history = chatMessageMapper.selectBySessionId(sessionId);
        
        // 构建消息列表（用于LLM API）
        List<Map<String, String>> messages = new ArrayList<>();
        
        // 1. 添加系统提示词（放在最前面，用于设定AI的角色和行为）
        String systemPrompt = llmConfig.getChat() != null && 
                             llmConfig.getChat().getSystemPrompt() != null ?
                             llmConfig.getChat().getSystemPrompt() :
                             "你是一个专业的智能客服助手，能够准确理解用户的问题并提供有帮助的回答。请用友好、专业、简洁的方式回答问题。如果用户提到之前对话的内容，请根据上下文进行回答。";
        
        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", systemPrompt);
        messages.add(systemMessage);
        
        // 2. 添加历史消息（构建对话上下文）
        // 获取配置的最大历史消息数量
        int maxHistory = llmConfig.getChat() != null && 
                        llmConfig.getChat().getMaxHistory() != null ?
                        llmConfig.getChat().getMaxHistory() : 30;
        
        // 限制历史消息数量，避免上下文过长
        // 每轮对话通常包含：user消息 + assistant消息
        // 所以maxHistory=30意味着保留最近30条消息（约15轮对话）
        
        // 计算起始索引，保留最近的消息（如果历史消息总数超过maxHistory，只保留最近的消息）
        int startIndex = Math.max(0, history.size() - maxHistory);
        
        // 按顺序添加历史消息（最早的在前，最新的在后）
        // 这样LLM可以按照时间顺序理解对话上下文
        for (int i = startIndex; i < history.size(); i++) {
            ChatMessage msg = history.get(i);
            
            // 确保消息格式正确（跳过格式不正确的消息）
            if (msg.getRole() != null && msg.getContent() != null && !msg.getContent().trim().isEmpty()) {
                Map<String, String> message = new HashMap<>();
                message.put("role", msg.getRole()); // user, assistant, system
                message.put("content", msg.getContent());
                messages.add(message);
            }
        }
        
        // 注意：不需要再次添加当前用户消息，因为：
        // 1. 用户消息已经保存到数据库（sendMessage方法第142-151行）
        // 2. 历史消息查询（第236行）已经包含了刚保存的用户消息
        // 3. 如果再次添加会导致重复，影响LLM的理解
        
        // 3. 调用LLM API生成回复
        // LLM会根据完整的上下文（系统提示词 + 历史消息）生成回复
        // 这样AI就能"记住"之前的对话内容，实现上下文记忆功能
        return llmService.generateResponse(messages);
    }
}

