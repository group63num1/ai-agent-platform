package com.example.demo.controller;

import com.example.demo.common.ApiResponse;
import com.example.demo.dto.*;
import com.example.demo.service.KnowledgeBaseService;
import com.example.demo.util.JwtUtil;
import com.example.demo.util.LogSafeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/knowledge-bases")
public class KnowledgeBaseController {

    private static final Logger logger = LoggerFactory.getLogger(KnowledgeBaseController.class);
    private static final int MAX_LOG_BODY_LEN = 4000;

    @Autowired
    private KnowledgeBaseService knowledgeBaseService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping
    public ApiResponse<KnowledgeBaseDTO> create(@RequestBody KnowledgeBaseCreateRequest request,
                                                HttpServletRequest httpRequest) {
        String rid = MDC.get("rid");
        logger.info("KB_HTTP_IN rid={} api=create method=POST uri=/api/knowledge-bases userId=? body={}",
                rid, LogSafeUtil.truncate(LogSafeUtil.toJson(request), MAX_LOG_BODY_LEN));
        try {
            Long userId = requireUserId(httpRequest);
            KnowledgeBaseDTO dto = knowledgeBaseService.create(userId, request);
            ApiResponse<KnowledgeBaseDTO> resp = ApiResponse.ok(dto);
            logger.info("KB_HTTP_OUT rid={} api=create code={} body={}",
                    rid, resp.getCode(), LogSafeUtil.truncate(LogSafeUtil.toJson(resp), MAX_LOG_BODY_LEN));
            return resp;
        } catch (IllegalStateException e) {
            ApiResponse<KnowledgeBaseDTO> resp = ApiResponse.fail(401, e.getMessage());
            logger.warn("KB_HTTP_OUT rid={} api=create code={} msg={}",
                    rid, resp.getCode(), LogSafeUtil.truncate(resp.getMessage(), MAX_LOG_BODY_LEN));
            return resp;
        } catch (Exception e) {
            ApiResponse<KnowledgeBaseDTO> resp = ApiResponse.fail(400, e.getMessage());
            logger.warn("KB_HTTP_OUT rid={} api=create code={} msg={}",
                    rid, resp.getCode(), LogSafeUtil.truncate(resp.getMessage(), MAX_LOG_BODY_LEN), e);
            return resp;
        }
    }

    @GetMapping
    public ApiResponse<KnowledgeBaseListResponse> list(@RequestParam(value = "keyword", required = false) String keyword,
                                                       @RequestParam(value = "page", required = false) Integer page,
                                                       @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        String rid = MDC.get("rid");
        logger.info("KB_HTTP_IN rid={} api=list method=GET uri=/api/knowledge-bases keyword={} page={} pageSize={}",
                rid, LogSafeUtil.truncate(keyword, 200), page, pageSize);
        KnowledgeBaseListResponse data = knowledgeBaseService.list(keyword, page == null ? 1 : page, pageSize == null ? 20 : pageSize);
        ApiResponse<KnowledgeBaseListResponse> resp = ApiResponse.ok(data);
        logger.info("KB_HTTP_OUT rid={} api=list code={} body={}",
                rid, resp.getCode(), LogSafeUtil.truncate(LogSafeUtil.toJson(resp), MAX_LOG_BODY_LEN));
        return resp;
    }

    @GetMapping("/{kbId}")
    public ApiResponse<KnowledgeBaseDTO> detail(@PathVariable String kbId) {
        String rid = MDC.get("rid");
        logger.info("KB_HTTP_IN rid={} api=detail method=GET uri=/api/knowledge-bases/{}",
                rid, LogSafeUtil.truncate(kbId, 200));
        try {
            ApiResponse<KnowledgeBaseDTO> resp = ApiResponse.ok(knowledgeBaseService.get(kbId));
            logger.info("KB_HTTP_OUT rid={} api=detail code={} body={}",
                    rid, resp.getCode(), LogSafeUtil.truncate(LogSafeUtil.toJson(resp), MAX_LOG_BODY_LEN));
            return resp;
        } catch (Exception e) {
            ApiResponse<KnowledgeBaseDTO> resp = ApiResponse.fail(404, e.getMessage());
            logger.warn("KB_HTTP_OUT rid={} api=detail code={} msg={}",
                    rid, resp.getCode(), LogSafeUtil.truncate(resp.getMessage(), MAX_LOG_BODY_LEN));
            return resp;
        }
    }

    @PutMapping("/{kbId}")
    public ApiResponse<KnowledgeBaseDTO> update(@PathVariable String kbId,
                                                @RequestBody KnowledgeBaseUpdateRequest request) {
        String rid = MDC.get("rid");
        logger.info("KB_HTTP_IN rid={} api=update method=PUT uri=/api/knowledge-bases/{} body={}",
                rid, LogSafeUtil.truncate(kbId, 200), LogSafeUtil.truncate(LogSafeUtil.toJson(request), MAX_LOG_BODY_LEN));
        try {
            ApiResponse<KnowledgeBaseDTO> resp = ApiResponse.ok(knowledgeBaseService.update(kbId, request));
            logger.info("KB_HTTP_OUT rid={} api=update code={} body={}",
                    rid, resp.getCode(), LogSafeUtil.truncate(LogSafeUtil.toJson(resp), MAX_LOG_BODY_LEN));
            return resp;
        } catch (Exception e) {
            ApiResponse<KnowledgeBaseDTO> resp = ApiResponse.fail(400, e.getMessage());
            logger.warn("KB_HTTP_OUT rid={} api=update code={} msg={}",
                    rid, resp.getCode(), LogSafeUtil.truncate(resp.getMessage(), MAX_LOG_BODY_LEN), e);
            return resp;
        }
    }

    @DeleteMapping("/{kbId}")
    public ApiResponse<?> delete(@PathVariable String kbId) {
        String rid = MDC.get("rid");
        logger.info("KB_HTTP_IN rid={} api=delete method=DELETE uri=/api/knowledge-bases/{}",
                rid, LogSafeUtil.truncate(kbId, 200));
        try {
            knowledgeBaseService.delete(kbId);
            ApiResponse<?> resp = ApiResponse.ok();
            logger.info("KB_HTTP_OUT rid={} api=delete code={} body={}",
                    rid, resp.getCode(), LogSafeUtil.truncate(LogSafeUtil.toJson(resp), MAX_LOG_BODY_LEN));
            return resp;
        } catch (Exception e) {
            ApiResponse<?> resp = ApiResponse.fail(400, e.getMessage());
            logger.warn("KB_HTTP_OUT rid={} api=delete code={} msg={}",
                    rid, resp.getCode(), LogSafeUtil.truncate(resp.getMessage(), MAX_LOG_BODY_LEN), e);
            return resp;
        }
    }

    @GetMapping("/{kbId}/documents")
    public ApiResponse<List<KbDocumentDTO>> listDocs(@PathVariable String kbId) {
        String rid = MDC.get("rid");
        logger.info("KB_HTTP_IN rid={} api=listDocs method=GET uri=/api/knowledge-bases/{}/documents",
                rid, LogSafeUtil.truncate(kbId, 200));
        try {
            ApiResponse<List<KbDocumentDTO>> resp = ApiResponse.ok(knowledgeBaseService.listDocuments(kbId));
            logger.info("KB_HTTP_OUT rid={} api=listDocs code={} body={}",
                    rid, resp.getCode(), LogSafeUtil.truncate(LogSafeUtil.toJson(resp), MAX_LOG_BODY_LEN));
            return resp;
        } catch (Exception e) {
            ApiResponse<List<KbDocumentDTO>> resp = ApiResponse.fail(400, e.getMessage());
            logger.warn("KB_HTTP_OUT rid={} api=listDocs code={} msg={}",
                    rid, resp.getCode(), LogSafeUtil.truncate(resp.getMessage(), MAX_LOG_BODY_LEN), e);
            return resp;
        }
    }

    @PostMapping("/{kbId}/documents")
    public ApiResponse<KbDocumentDTO> uploadDoc(@PathVariable String kbId,
                                                @RequestPart("file") MultipartFile file,
                                                @RequestParam(value = "splitMethod", required = false) String splitMethod,
                                                @RequestParam(value = "chunkSize", required = false) Integer chunkSize) {
        String rid = MDC.get("rid");
        String filename = file == null ? null : file.getOriginalFilename();
        Long size = null;
        try {
            size = file == null ? null : file.getSize();
        } catch (Exception ignore) {}
        logger.info("KB_HTTP_IN rid={} api=uploadDoc method=POST uri=/api/knowledge-bases/{}/documents filename={} size={} splitMethod={} chunkSize={}",
                rid, LogSafeUtil.truncate(kbId, 200), LogSafeUtil.truncate(filename, 500), size, LogSafeUtil.truncate(splitMethod, 100), chunkSize);
        try {
            ApiResponse<KbDocumentDTO> resp = ApiResponse.ok(knowledgeBaseService.uploadDocument(kbId, file, splitMethod, chunkSize));
            logger.info("KB_HTTP_OUT rid={} api=uploadDoc code={} body={}",
                    rid, resp.getCode(), LogSafeUtil.truncate(LogSafeUtil.toJson(resp), MAX_LOG_BODY_LEN));
            return resp;
        } catch (IOException e) {
            ApiResponse<KbDocumentDTO> resp = ApiResponse.fail(400, "文件读取失败: " + e.getMessage());
            logger.warn("KB_HTTP_OUT rid={} api=uploadDoc code={} msg={}",
                    rid, resp.getCode(), LogSafeUtil.truncate(resp.getMessage(), MAX_LOG_BODY_LEN), e);
            return resp;
        } catch (Exception e) {
            ApiResponse<KbDocumentDTO> resp = ApiResponse.fail(400, e.getMessage());
            logger.warn("KB_HTTP_OUT rid={} api=uploadDoc code={} msg={}",
                    rid, resp.getCode(), LogSafeUtil.truncate(resp.getMessage(), MAX_LOG_BODY_LEN), e);
            return resp;
        }
    }

    @DeleteMapping("/{kbId}/documents/{docId}")
    public ApiResponse<?> deleteDoc(@PathVariable String kbId, @PathVariable Long docId) {
        String rid = MDC.get("rid");
        logger.info("KB_HTTP_IN rid={} api=deleteDoc method=DELETE uri=/api/knowledge-bases/{}/documents/{}",
                rid, LogSafeUtil.truncate(kbId, 200), docId);
        try {
            knowledgeBaseService.deleteDocument(kbId, docId);
            ApiResponse<?> resp = ApiResponse.ok();
            logger.info("KB_HTTP_OUT rid={} api=deleteDoc code={} body={}",
                    rid, resp.getCode(), LogSafeUtil.truncate(LogSafeUtil.toJson(resp), MAX_LOG_BODY_LEN));
            return resp;
        } catch (Exception e) {
            ApiResponse<?> resp = ApiResponse.fail(400, e.getMessage());
            logger.warn("KB_HTTP_OUT rid={} api=deleteDoc code={} msg={}",
                    rid, resp.getCode(), LogSafeUtil.truncate(resp.getMessage(), MAX_LOG_BODY_LEN), e);
            return resp;
        }
    }

    @PostMapping("/{kbId}/search")
    public ApiResponse<KnowledgeBaseSearchResponse> search(@PathVariable String kbId,
                                                           @RequestBody KnowledgeBaseSearchRequest request) {
        String rid = MDC.get("rid");
        logger.info("KB_HTTP_IN rid={} api=search method=POST uri=/api/knowledge-bases/{}/search body={}",
                rid, LogSafeUtil.truncate(kbId, 200), LogSafeUtil.truncate(LogSafeUtil.toJson(request), MAX_LOG_BODY_LEN));
        try {
            ApiResponse<KnowledgeBaseSearchResponse> resp = ApiResponse.ok(knowledgeBaseService.search(kbId, request));
            logger.info("KB_HTTP_OUT rid={} api=search code={} body={}",
                    rid, resp.getCode(), LogSafeUtil.truncate(LogSafeUtil.toJson(resp), MAX_LOG_BODY_LEN));
            return resp;
        } catch (Exception e) {
            ApiResponse<KnowledgeBaseSearchResponse> resp = ApiResponse.fail(400, e.getMessage());
            logger.warn("KB_HTTP_OUT rid={} api=search code={} msg={}",
                    rid, resp.getCode(), LogSafeUtil.truncate(resp.getMessage(), MAX_LOG_BODY_LEN), e);
            return resp;
        }
    }

    /**
     * 获取已启用的知识库名称列表
     * GET /api/knowledge-bases/getlist
     */
    @GetMapping("/getlist")
    public ApiResponse<List<String>> listEnabledNames() {
        String rid = MDC.get("rid");
        logger.info("KB_HTTP_IN rid={} api=listEnabledNames method=GET uri=/api/knowledge-bases/getlist", rid);
        try {
            ApiResponse<List<String>> resp = ApiResponse.ok(knowledgeBaseService.listEnabledNames());
            logger.info("KB_HTTP_OUT rid={} api=listEnabledNames code={} body={}",
                    rid, resp.getCode(), LogSafeUtil.truncate(LogSafeUtil.toJson(resp), MAX_LOG_BODY_LEN));
            return resp;
        } catch (Exception e) {
            ApiResponse<List<String>> resp = ApiResponse.fail(400, e.getMessage());
            logger.warn("KB_HTTP_OUT rid={} api=listEnabledNames code={} msg={}",
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

