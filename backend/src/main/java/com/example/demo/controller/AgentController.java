package com.example.demo.controller;

import com.example.demo.common.ApiResponse;
import com.example.demo.dto.*;
import com.example.demo.service.AgentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import com.example.demo.util.JwtUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

@RestController
@RequestMapping("/api/agents")
public class AgentController {

    @Autowired
    private AgentService agentService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping
    public ApiResponse<AgentDTO> createAgent(@RequestBody AgentCreateRequest request,
                                             HttpServletRequest httpRequest) {
        try {
            Long userId = requireUserId(httpRequest);
            return ApiResponse.ok(agentService.createAgent(userId, request));
        } catch (IllegalStateException e) {
            return ApiResponse.fail(401, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    @GetMapping
    public ApiResponse<AgentListResponse> listAgents(@RequestParam(defaultValue = "1") int page,
                                                     @RequestParam(defaultValue = "20") int pageSize,
                                                     @RequestParam(required = false) String keyword) {
        try {
            return ApiResponse.ok(agentService.listAgents(page, pageSize, keyword));
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    @GetMapping("/published")
    public ApiResponse<List<AgentDTO>> listPublishedAgents() {
        try {
            return ApiResponse.ok(agentService.listPublishedAgents());
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ApiResponse<AgentDTO> getAgent(@PathVariable("id") String id) {
        try {
            return ApiResponse.ok(agentService.getAgent(id));
        } catch (Exception e) {
            return ApiResponse.fail(404, e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ApiResponse<AgentDTO> updateAgent(@PathVariable("id") String id,
                                             @RequestBody AgentUpdateRequest request) {
        try {
            return ApiResponse.ok(agentService.updateAgent(id, request));
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteAgent(@PathVariable("id") String id) {
        try {
            agentService.deleteAgent(id);
            return ApiResponse.ok();
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    @PostMapping("/{id}/publish")
    public ApiResponse<AgentDTO> publishAgent(@PathVariable("id") String id) {
        try {
            return ApiResponse.ok(agentService.publishAgent(id));
        } catch (Exception e) {
            return ApiResponse.fail(409, e.getMessage());
        }
    }

    @PostMapping("/{id}/unpublish")
    public ApiResponse<AgentDTO> unpublishAgent(@PathVariable("id") String id) {
        try {
            return ApiResponse.ok(agentService.unpublishAgent(id));
        } catch (Exception e) {
            return ApiResponse.fail(409, e.getMessage());
        }
    }

    @PostMapping("/{id}/sessions")
    public ApiResponse<AgentSessionDTO> createSession(@PathVariable("id") String id,
                                                      @RequestBody(required = false) AgentSessionCreateRequest request) {
        try {
            return ApiResponse.ok(agentService.createSession(id, request));
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    @DeleteMapping("/{id}/sessions/{sessionId}")
    public ApiResponse<Void> deleteSession(@PathVariable("id") String id,
                                           @PathVariable("sessionId") String sessionId) {
        try {
            agentService.deleteSession(id, sessionId);
            return ApiResponse.ok();
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    @GetMapping("/{id}/sessions")
    public ApiResponse<List<AgentSessionDTO>> listSessions(@PathVariable("id") String id) {
        try {
            return ApiResponse.ok(agentService.listSessions(id));
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    @PutMapping("/{id}/sessions/{sessionId}")
    public ApiResponse<AgentSessionDTO> updateSession(@PathVariable("id") String id,
                                                      @PathVariable("sessionId") String sessionId,
                                                      @RequestBody AgentSessionUpdateRequest request) {
        try {
            return ApiResponse.ok(agentService.updateSession(id, sessionId, request));
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    @PostMapping(value = "/{id}/sessions/{sessionId}/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@PathVariable("id") String id,
                           @PathVariable("sessionId") String sessionId,
                           @RequestBody AgentChatRequest request) {
        return agentService.chatWithAgent(id, sessionId, request);
    }

    @GetMapping("/{id}/sessions/{sessionId}/messages")
    public ApiResponse<List<AgentMessageDTO>> listMessages(@PathVariable("id") String id,
                                                           @PathVariable("sessionId") String sessionId,
                                                           @RequestParam(required = false) Integer limit) {
        try {
            return ApiResponse.ok(agentService.listMessages(id, sessionId, limit));
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    private Long requireUserId(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Long) {
            return (Long) authentication.getPrincipal();
        }
        String bearer = request == null ? null : request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            String token = bearer.substring(7);
            if (jwtUtil.validateToken(token)) {
                Long userId = jwtUtil.getUserIdFromToken(token);
                if (userId != null) {
                    return userId;
                }
            }
        }
        throw new IllegalStateException("未登录或token无效");
    }
}


