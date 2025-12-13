package com.example.demo.controller;

import com.example.demo.common.ApiResponse;
import com.example.demo.dto.*;
import com.example.demo.service.WorkflowService;
import com.example.demo.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/workflows")
public class WorkflowController {

    @Autowired
    private WorkflowService workflowService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping
    public ApiResponse<List<WorkflowDTO>> listMy(HttpServletRequest request) {
        try {
            Long userId = requireUserId(request);
            return ApiResponse.ok(workflowService.listMyWorkflows(userId));
        } catch (IllegalStateException e) {
            return ApiResponse.fail(401, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    @PostMapping
    public ApiResponse<WorkflowDTO> create(@RequestBody WorkflowCreateRequest body,
                                          HttpServletRequest request) {
        try {
            Long userId = requireUserId(request);
            return ApiResponse.ok(workflowService.createWorkflow(userId, body));
        } catch (IllegalStateException e) {
            return ApiResponse.fail(401, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    @PostMapping("/{id}/save")
    public ApiResponse<WorkflowDTO> save(@PathVariable("id") Long id,
                                        @RequestBody WorkflowSaveRequest body,
                                        HttpServletRequest request) {
        try {
            Long userId = requireUserId(request);
            return ApiResponse.ok(workflowService.saveWorkflow(userId, id, body));
        } catch (IllegalStateException e) {
            return ApiResponse.fail(401, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ApiResponse<WorkflowDTO> detail(@PathVariable("id") Long id,
                                          HttpServletRequest request) {
        try {
            Long userId = requireUserId(request);
            return ApiResponse.ok(workflowService.getWorkflowDetail(userId, id));
        } catch (IllegalStateException e) {
            return ApiResponse.fail(401, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") Long id,
                                    HttpServletRequest request) {
        try {
            Long userId = requireUserId(request);
            workflowService.deleteWorkflow(userId, id);
            return ApiResponse.ok();
        } catch (IllegalStateException e) {
            return ApiResponse.fail(401, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    @PostMapping("/{id}/execute")
    public ApiResponse<WorkflowExecuteResultDTO> execute(@PathVariable("id") Long id,
                                                        @RequestBody WorkflowExecuteRequest body,
                                                        HttpServletRequest request) {
        try {
            Long userId = requireUserId(request);
            return ApiResponse.ok(workflowService.executeWorkflow(userId, id, body));
        } catch (IllegalStateException e) {
            return ApiResponse.fail(401, e.getMessage());
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
