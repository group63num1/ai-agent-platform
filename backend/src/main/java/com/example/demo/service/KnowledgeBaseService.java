package com.example.demo.service;

import com.example.demo.dto.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface KnowledgeBaseService {
    KnowledgeBaseDTO create(Long userId, KnowledgeBaseCreateRequest request) throws IOException;

    KnowledgeBaseListResponse list(String keyword, int page, int pageSize);

    KnowledgeBaseDTO get(String kbId);

    KnowledgeBaseDTO update(String kbId, KnowledgeBaseUpdateRequest request);

    void delete(String kbId);

    List<KbDocumentDTO> listDocuments(String kbId);

    KbDocumentDTO uploadDocument(String kbId, MultipartFile file, String splitMethod, Integer chunkSize) throws IOException;

    void deleteDocument(String kbId, Long docId);

    KnowledgeBaseSearchResponse search(String kbId, KnowledgeBaseSearchRequest request);

    /**
     * 获取已启用的知识库名称列表
     */
    List<String> listEnabledNames();
}

