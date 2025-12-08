package com.example.demo.controller;

import com.example.demo.common.ApiResponse;
import com.example.demo.dto.*;
import com.example.demo.service.KnowledgeBaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/knowledge-bases")
public class KnowledgeBaseController {

    @Autowired
    private KnowledgeBaseService knowledgeBaseService;

    @PostMapping
    public ApiResponse<KnowledgeBaseDTO> create(@RequestBody KnowledgeBaseCreateRequest request,
                                                @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        try {
            KnowledgeBaseDTO dto = knowledgeBaseService.create(userId, request);
            return ApiResponse.ok(dto);
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    @GetMapping
    public ApiResponse<KnowledgeBaseListResponse> list(@RequestParam(value = "keyword", required = false) String keyword,
                                                       @RequestParam(value = "page", required = false) Integer page,
                                                       @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        KnowledgeBaseListResponse resp = knowledgeBaseService.list(keyword, page == null ? 1 : page, pageSize == null ? 20 : pageSize);
        return ApiResponse.ok(resp);
    }

    @GetMapping("/{kbId}")
    public ApiResponse<KnowledgeBaseDTO> detail(@PathVariable String kbId) {
        try {
            return ApiResponse.ok(knowledgeBaseService.get(kbId));
        } catch (Exception e) {
            return ApiResponse.fail(404, e.getMessage());
        }
    }

    @PutMapping("/{kbId}")
    public ApiResponse<KnowledgeBaseDTO> update(@PathVariable String kbId,
                                                @RequestBody KnowledgeBaseUpdateRequest request) {
        try {
            return ApiResponse.ok(knowledgeBaseService.update(kbId, request));
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    @DeleteMapping("/{kbId}")
    public ApiResponse<?> delete(@PathVariable String kbId) {
        try {
            knowledgeBaseService.delete(kbId);
            return ApiResponse.ok();
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    @GetMapping("/{kbId}/documents")
    public ApiResponse<List<KbDocumentDTO>> listDocs(@PathVariable String kbId) {
        try {
            return ApiResponse.ok(knowledgeBaseService.listDocuments(kbId));
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    @PostMapping("/{kbId}/documents")
    public ApiResponse<KbDocumentDTO> uploadDoc(@PathVariable String kbId,
                                                @RequestPart("file") MultipartFile file,
                                                @RequestParam(value = "splitMethod", required = false) String splitMethod,
                                                @RequestParam(value = "chunkSize", required = false) Integer chunkSize) {
        try {
            return ApiResponse.ok(knowledgeBaseService.uploadDocument(kbId, file, splitMethod, chunkSize));
        } catch (IOException e) {
            return ApiResponse.fail(400, "文件读取失败: " + e.getMessage());
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    @DeleteMapping("/{kbId}/documents/{docId}")
    public ApiResponse<?> deleteDoc(@PathVariable String kbId, @PathVariable Long docId) {
        try {
            knowledgeBaseService.deleteDocument(kbId, docId);
            return ApiResponse.ok();
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    @PostMapping("/{kbId}/search")
    public ApiResponse<KnowledgeBaseSearchResponse> search(@PathVariable String kbId,
                                                           @RequestBody KnowledgeBaseSearchRequest request) {
        try {
            return ApiResponse.ok(knowledgeBaseService.search(kbId, request));
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    /**
     * 获取已启用的知识库名称列表
     * GET /api/knowledge-bases/getlist
     */
    @GetMapping("/getlist")
    public ApiResponse<List<String>> listEnabledNames() {
        try {
            return ApiResponse.ok(knowledgeBaseService.listEnabledNames());
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }
}

