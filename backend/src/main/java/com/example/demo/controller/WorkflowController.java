package com.example.demo.controller;

import com.example.demo.common.ApiResponse;
import com.example.demo.dto.*;
import com.example.demo.service.WorkflowService;
import com.example.demo.util.JwtUtil;
import com.example.demo.util.LogSafeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/workflows")
public class WorkflowController {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowController.class);
    private static final int MAX_LOG_BODY_LEN = 4000;

    @Autowired
    private WorkflowService workflowService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping
    public ApiResponse<List<WorkflowDTO>> listMy(HttpServletRequest request) {
        String rid = MDC.get("rid");
        logger.info("WF_HTTP_IN rid={} api=listMy method=GET uri=/api/workflows", rid);
        try {
            Long userId = requireUserId(request);
            ApiResponse<List<WorkflowDTO>> resp = ApiResponse.ok(workflowService.listMyWorkflows(userId));
            logger.info("WF_HTTP_OUT rid={} api=listMy code={} body={}",
                    rid, resp.getCode(), LogSafeUtil.truncate(LogSafeUtil.toJson(resp), MAX_LOG_BODY_LEN));
            return resp;
        } catch (IllegalStateException e) {
            ApiResponse<List<WorkflowDTO>> resp = ApiResponse.fail(401, e.getMessage());
            logger.warn("WF_HTTP_OUT rid={} api=listMy code={} msg={}",
                    rid, resp.getCode(), LogSafeUtil.truncate(resp.getMessage(), MAX_LOG_BODY_LEN));
            return resp;
        } catch (Exception e) {
            ApiResponse<List<WorkflowDTO>> resp = ApiResponse.fail(400, e.getMessage());
            logger.warn("WF_HTTP_OUT rid={} api=listMy code={} msg={}",
                    rid, resp.getCode(), LogSafeUtil.truncate(resp.getMessage(), MAX_LOG_BODY_LEN), e);
            return resp;
        }
    }

    @PostMapping
    public ApiResponse<WorkflowDTO> create(@RequestBody WorkflowCreateRequest body,
                                          HttpServletRequest request) {
        String rid = MDC.get("rid");
        logger.info("WF_HTTP_IN rid={} api=create method=POST uri=/api/workflows body={}",
                rid, LogSafeUtil.truncate(LogSafeUtil.toJson(body), MAX_LOG_BODY_LEN));
        try {
            Long userId = requireUserId(request);
            ApiResponse<WorkflowDTO> resp = ApiResponse.ok(workflowService.createWorkflow(userId, body));
            logger.info("WF_HTTP_OUT rid={} api=create code={} body={}",
                    rid, resp.getCode(), LogSafeUtil.truncate(LogSafeUtil.toJson(resp), MAX_LOG_BODY_LEN));
            return resp;
        } catch (IllegalStateException e) {
            ApiResponse<WorkflowDTO> resp = ApiResponse.fail(401, e.getMessage());
            logger.warn("WF_HTTP_OUT rid={} api=create code={} msg={}",
                    rid, resp.getCode(), LogSafeUtil.truncate(resp.getMessage(), MAX_LOG_BODY_LEN));
            return resp;
        } catch (Exception e) {
            ApiResponse<WorkflowDTO> resp = ApiResponse.fail(400, e.getMessage());
            logger.warn("WF_HTTP_OUT rid={} api=create code={} msg={}",
                    rid, resp.getCode(), LogSafeUtil.truncate(resp.getMessage(), MAX_LOG_BODY_LEN), e);
            return resp;
        }
    }

    @PostMapping("/{id}/save")
    public ApiResponse<WorkflowDTO> save(@PathVariable("id") Long id,
                                        @RequestBody WorkflowSaveRequest body,
                                        HttpServletRequest request) {
        String rid = MDC.get("rid");
        logger.info("WF_HTTP_IN rid={} api=save method=POST uri=/api/workflows/{}/save body={}",
                rid, id, LogSafeUtil.truncate(LogSafeUtil.toJson(body), MAX_LOG_BODY_LEN));
        try {
            Long userId = requireUserId(request);
            ApiResponse<WorkflowDTO> resp = ApiResponse.ok(workflowService.saveWorkflow(userId, id, body));
            logger.info("WF_HTTP_OUT rid={} api=save code={} body={}",
                    rid, resp.getCode(), LogSafeUtil.truncate(LogSafeUtil.toJson(resp), MAX_LOG_BODY_LEN));
            return resp;
        } catch (IllegalStateException e) {
            ApiResponse<WorkflowDTO> resp = ApiResponse.fail(401, e.getMessage());
            logger.warn("WF_HTTP_OUT rid={} api=save code={} msg={}",
                    rid, resp.getCode(), LogSafeUtil.truncate(resp.getMessage(), MAX_LOG_BODY_LEN));
            return resp;
        } catch (Exception e) {
            ApiResponse<WorkflowDTO> resp = ApiResponse.fail(400, e.getMessage());
            logger.warn("WF_HTTP_OUT rid={} api=save code={} msg={}",
                    rid, resp.getCode(), LogSafeUtil.truncate(resp.getMessage(), MAX_LOG_BODY_LEN), e);
            return resp;
        }
    }

    @GetMapping("/{id}")
    public ApiResponse<WorkflowDTO> detail(@PathVariable("id") Long id,
                                          HttpServletRequest request) {
        String rid = MDC.get("rid");
        logger.info("WF_HTTP_IN rid={} api=detail method=GET uri=/api/workflows/{}",
                rid, id);
        try {
            Long userId = requireUserId(request);
            ApiResponse<WorkflowDTO> resp = ApiResponse.ok(workflowService.getWorkflowDetail(userId, id));
            logger.info("WF_HTTP_OUT rid={} api=detail code={} body={}",
                    rid, resp.getCode(), LogSafeUtil.truncate(LogSafeUtil.toJson(resp), MAX_LOG_BODY_LEN));
            return resp;
        } catch (IllegalStateException e) {
            ApiResponse<WorkflowDTO> resp = ApiResponse.fail(401, e.getMessage());
            logger.warn("WF_HTTP_OUT rid={} api=detail code={} msg={}",
                    rid, resp.getCode(), LogSafeUtil.truncate(resp.getMessage(), MAX_LOG_BODY_LEN));
            return resp;
        } catch (Exception e) {
            ApiResponse<WorkflowDTO> resp = ApiResponse.fail(400, e.getMessage());
            logger.warn("WF_HTTP_OUT rid={} api=detail code={} msg={}",
                    rid, resp.getCode(), LogSafeUtil.truncate(resp.getMessage(), MAX_LOG_BODY_LEN), e);
            return resp;
        }
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") Long id,
                                    HttpServletRequest request) {
        String rid = MDC.get("rid");
        logger.info("WF_HTTP_IN rid={} api=delete method=DELETE uri=/api/workflows/{}",
                rid, id);
        try {
            Long userId = requireUserId(request);
            workflowService.deleteWorkflow(userId, id);
            ApiResponse<Void> resp = ApiResponse.ok();
            logger.info("WF_HTTP_OUT rid={} api=delete code={} body={}",
                    rid, resp.getCode(), LogSafeUtil.truncate(LogSafeUtil.toJson(resp), MAX_LOG_BODY_LEN));
            return resp;
        } catch (IllegalStateException e) {
            ApiResponse<Void> resp = ApiResponse.fail(401, e.getMessage());
            logger.warn("WF_HTTP_OUT rid={} api=delete code={} msg={}",
                    rid, resp.getCode(), LogSafeUtil.truncate(resp.getMessage(), MAX_LOG_BODY_LEN));
            return resp;
        } catch (Exception e) {
            ApiResponse<Void> resp = ApiResponse.fail(400, e.getMessage());
            logger.warn("WF_HTTP_OUT rid={} api=delete code={} msg={}",
                    rid, resp.getCode(), LogSafeUtil.truncate(resp.getMessage(), MAX_LOG_BODY_LEN), e);
            return resp;
        }
    }

    @PostMapping("/{id}/execute")
    public ApiResponse<WorkflowExecuteResultDTO> execute(@PathVariable("id") Long id,
                                                        @RequestBody WorkflowExecuteRequest body,
                                                        HttpServletRequest request) {
        String rid = MDC.get("rid");
        logger.info("WF_HTTP_IN rid={} api=execute method=POST uri=/api/workflows/{}/execute body={}",
                rid, id, LogSafeUtil.truncate(LogSafeUtil.toJson(body), MAX_LOG_BODY_LEN));
        try {
            Long userId = requireUserId(request);
            ApiResponse<WorkflowExecuteResultDTO> resp = ApiResponse.ok(workflowService.executeWorkflow(userId, id, body));
            logger.info("WF_HTTP_OUT rid={} api=execute code={} body={}",
                    rid, resp.getCode(), LogSafeUtil.truncate(LogSafeUtil.toJson(resp), MAX_LOG_BODY_LEN));
            return resp;
        } catch (IllegalStateException e) {
            ApiResponse<WorkflowExecuteResultDTO> resp = ApiResponse.fail(401, e.getMessage());
            logger.warn("WF_HTTP_OUT rid={} api=execute code={} msg={}",
                    rid, resp.getCode(), LogSafeUtil.truncate(resp.getMessage(), MAX_LOG_BODY_LEN));
            return resp;
        } catch (Exception e) {
            ApiResponse<WorkflowExecuteResultDTO> resp = ApiResponse.fail(400, e.getMessage());
            logger.warn("WF_HTTP_OUT rid={} api=execute code={} msg={}",
                    rid, resp.getCode(), LogSafeUtil.truncate(resp.getMessage(), MAX_LOG_BODY_LEN), e);
            return resp;
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
