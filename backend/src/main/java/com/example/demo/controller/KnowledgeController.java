// src/main/java/com/example/demo/controller/KnowledgeController.java
package com.example.demo.controller;

import com.example.demo.common.ApiResponse;
import com.example.demo.dto.*;
import com.example.demo.service.KnowledgeService;
import com.example.demo.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/knowledge")
public class KnowledgeController {

    @Autowired
    private KnowledgeService knowledgeService;

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
     * 创建知识库文档（上传文档并分片）
     * POST /api/v1/knowledge/documents
     */
    @PostMapping("/documents")
    public ApiResponse<KnowledgeDocumentDTO> createDocument(
            @Valid @RequestBody KnowledgeDocumentCreateDTO createDTO,
            HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            KnowledgeDocumentDTO document = knowledgeService.createDocument(userId, createDTO);
            return ApiResponse.ok(document);
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    /**
     * 获取用户的文档列表
     * GET /api/v1/knowledge/documents
     */
    @GetMapping("/documents")
    public ApiResponse<List<KnowledgeDocumentDTO>> getUserDocuments(HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            List<KnowledgeDocumentDTO> documents = knowledgeService.getUserDocuments(userId);
            return ApiResponse.ok(documents);
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    /**
     * 获取文档详情
     * GET /api/v1/knowledge/documents/{documentId}
     */
    @GetMapping("/documents/{documentId}")
    public ApiResponse<KnowledgeDocumentDTO> getDocument(
            @PathVariable Long documentId,
            HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            KnowledgeDocumentDTO document = knowledgeService.getDocumentById(documentId, userId);
            return ApiResponse.ok(document);
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    /**
     * 删除文档（软删除）
     * DELETE /api/v1/knowledge/documents/{documentId}
     */
    @DeleteMapping("/documents/{documentId}")
    public ApiResponse<Void> deleteDocument(
            @PathVariable Long documentId,
            HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            knowledgeService.deleteDocument(documentId, userId);
            return ApiResponse.ok(null);
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    /**
     * 获取文档的块列表
     * GET /api/v1/knowledge/documents/{documentId}/chunks
     */
    @GetMapping("/documents/{documentId}/chunks")
    public ApiResponse<List<KnowledgeChunkDTO>> getDocumentChunks(
            @PathVariable Long documentId,
            HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            List<KnowledgeChunkDTO> chunks = knowledgeService.getDocumentChunks(documentId, userId);
            return ApiResponse.ok(chunks);
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    /**
     * 检索知识库（根据查询内容检索相关文档块）
     * POST /api/v1/knowledge/search
     */
    @PostMapping("/search")
    public ApiResponse<KnowledgeSearchResultDTO> search(
            @Valid @RequestBody KnowledgeSearchDTO searchDTO,
            HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            KnowledgeSearchResultDTO result = knowledgeService.search(userId, searchDTO);
            return ApiResponse.ok(result);
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    /**
     * 基于知识库的问答（检索 + LLM生成答案）
     * POST /api/v1/knowledge/ask
     */
    @PostMapping("/ask")
    public ApiResponse<KnowledgeQAResponseDTO> ask(
            @Valid @RequestBody KnowledgeQADTO qaDTO,
            HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            KnowledgeQAResponseDTO response = knowledgeService.ask(userId, qaDTO);
            return ApiResponse.ok(response);
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }
}

