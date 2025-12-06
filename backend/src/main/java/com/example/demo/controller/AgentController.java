package com.example.demo.controller;

import com.example.demo.common.ApiResponse;
import com.example.demo.dto.AgentChatRequest;
import com.example.demo.dto.AgentDTO;
import com.example.demo.dto.AgentListResponse;
import com.example.demo.dto.AgentMessageDTO;
import com.example.demo.dto.AgentSessionCreateRequest;
import com.example.demo.dto.AgentSessionDTO;
import com.example.demo.dto.AgentUpdateRequest;
import com.example.demo.dto.AgentCreateRequest;
import com.example.demo.service.AgentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/agents")
public class AgentController {

    @Autowired
    private AgentService agentService;

    @PostMapping
    public ApiResponse<AgentDTO> createAgent(@RequestBody AgentCreateRequest request) {
        try {
            return ApiResponse.ok(agentService.createAgent(request));
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
}


