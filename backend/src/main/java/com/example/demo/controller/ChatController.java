// src/main/java/com/example/demo/controller/ChatController.java
package com.example.demo.controller;

import com.example.demo.common.ApiResponse;
import com.example.demo.dto.*;
import com.example.demo.service.ChatService;
import com.example.demo.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 从请求中获取 JWT Token
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        String token = getJwtFromRequest(request);
        if (token == null) {
            throw new RuntimeException("未授权访问");
        }
        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            throw new RuntimeException("未授权访问");
        }
        return userId;
    }

    /**
     * 创建会话
     * POST /api/v1/chat/sessions
     */
    @PostMapping("/sessions")
    public ApiResponse<ChatSessionDTO> createSession(
            @Valid @RequestBody ChatSessionCreateDTO createDTO,
            HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            ChatSessionDTO session = chatService.createSession(userId, createDTO);
            return ApiResponse.ok(session);
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    /**
     * 获取用户的会话列表
     * GET /api/v1/chat/sessions
     */
    @GetMapping("/sessions")
    public ApiResponse<List<ChatSessionDTO>> getUserSessions(HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            List<ChatSessionDTO> sessions = chatService.getUserSessions(userId);
            return ApiResponse.ok(sessions);
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    /**
     * 获取会话详情
     * GET /api/v1/chat/sessions/{sessionId}
     */
    @GetMapping("/sessions/{sessionId}")
    public ApiResponse<ChatSessionDTO> getSession(
            @PathVariable Long sessionId,
            HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            ChatSessionDTO session = chatService.getSessionById(sessionId, userId);
            return ApiResponse.ok(session);
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    /**
     * 删除会话
     * DELETE /api/v1/chat/sessions/{sessionId}
     */
    @DeleteMapping("/sessions/{sessionId}")
    public ApiResponse<Void> deleteSession(
            @PathVariable Long sessionId,
            HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            chatService.deleteSession(sessionId, userId);
            return ApiResponse.ok(null);
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    /**
     * 更新会话标题
     * PUT /api/v1/chat/sessions/{sessionId}/title
     */
    @PutMapping("/sessions/{sessionId}/title")
    public ApiResponse<ChatSessionDTO> updateSessionTitle(
            @PathVariable Long sessionId,
            @RequestParam String title,
            HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            ChatSessionDTO session = chatService.updateSessionTitle(sessionId, userId, title);
            return ApiResponse.ok(session);
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    /**
     * 发送消息
     * POST /api/v1/chat/messages
     */
    @PostMapping("/messages")
    public ApiResponse<ChatResponseDTO> sendMessage(
            @Valid @RequestBody ChatMessageCreateDTO messageDTO,
            HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            
            // 获取IP地址和User-Agent（用于安全检测）
            String ipAddress = getClientIpAddress(request);
            String userAgent = request.getHeader("User-Agent");
            
            ChatResponseDTO response = chatService.sendMessage(userId, messageDTO, ipAddress, userAgent);
            return ApiResponse.ok(response);
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 处理多个IP的情况，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip != null ? ip : "unknown";
    }

    /**
     * 获取会话的消息列表
     * GET /api/v1/chat/sessions/{sessionId}/messages
     */
    @GetMapping("/sessions/{sessionId}/messages")
    public ApiResponse<List<ChatMessageDTO>> getSessionMessages(
            @PathVariable Long sessionId,
            @RequestParam(required = false) Long lastMessageId,
            @RequestParam(required = false, defaultValue = "20") Integer limit,
            HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            List<ChatMessageDTO> messages = chatService.getSessionMessages(sessionId, userId, lastMessageId, limit);
            return ApiResponse.ok(messages);
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    /**
     * 获取会话的消息数量
     * GET /api/v1/chat/sessions/{sessionId}/message-count
     */
    @GetMapping("/sessions/{sessionId}/message-count")
    public ApiResponse<Integer> getMessageCount(
            @PathVariable Long sessionId,
            HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            int count = chatService.getMessageCount(sessionId, userId);
            return ApiResponse.ok(count);
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }
}

