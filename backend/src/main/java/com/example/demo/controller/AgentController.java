package com.example.demo.controller;

import com.example.demo.common.ApiResponse;
import com.example.demo.dto.*;
import com.example.demo.service.AgentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/{id}/chat")
    public ApiResponse<AgentChatResponse> chat(@PathVariable("id") String id,
                                               @RequestBody AgentChatRequest request) {
        try {
            return ApiResponse.ok(agentService.chatWithAgent(id, request));
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    @GetMapping("/{id}/chat/messages")
    public ApiResponse<List<AgentMessageDTO>> listMessages(@PathVariable("id") String id,
                                                           @RequestParam(required = false) Integer limit) {
        try {
            return ApiResponse.ok(agentService.listMessages(id, limit));
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }
}


