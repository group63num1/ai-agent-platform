// src/main/java/com/example/demo/service/ChatService.java
package com.example.demo.service;

import com.example.demo.dto.*;

import java.util.List;

public interface ChatService {

    // 创建会话
    ChatSessionDTO createSession(Long userId, ChatSessionCreateDTO createDTO);

    // 获取用户的会话列表
    List<ChatSessionDTO> getUserSessions(Long userId);

    // 获取会话详情（包含消息列表）
    ChatSessionDTO getSessionById(Long sessionId, Long userId);

    // 删除会话（软删除）
    void deleteSession(Long sessionId, Long userId);

    // 更新会话标题
    ChatSessionDTO updateSessionTitle(Long sessionId, Long userId, String title);

    // 发送消息并获取AI回复
    ChatResponseDTO sendMessage(Long userId, ChatMessageCreateDTO messageDTO, 
                                String ipAddress, String userAgent);

    // 获取会话的消息列表
    List<ChatMessageDTO> getSessionMessages(Long sessionId, Long userId, Long lastMessageId, Integer limit);

    // 获取会话的消息数量
    int getMessageCount(Long sessionId, Long userId);
}

