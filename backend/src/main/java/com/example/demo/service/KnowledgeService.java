// src/main/java/com/example/demo/service/KnowledgeService.java
package com.example.demo.service;

import com.example.demo.dto.*;

import java.util.List;

public interface KnowledgeService {

    /**
     * 创建知识库文档（上传文档并分片）
     * @param userId 用户ID
     * @param createDTO 文档创建DTO
     * @return 文档DTO
     */
    KnowledgeDocumentDTO createDocument(Long userId, KnowledgeDocumentCreateDTO createDTO);

    /**
     * 获取用户的文档列表
     * @param userId 用户ID
     * @return 文档列表
     */
    List<KnowledgeDocumentDTO> getUserDocuments(Long userId);

    /**
     * 获取文档详情
     * @param documentId 文档ID
     * @param userId 用户ID
     * @return 文档DTO
     */
    KnowledgeDocumentDTO getDocumentById(Long documentId, Long userId);

    /**
     * 删除文档（软删除）
     * @param documentId 文档ID
     * @param userId 用户ID
     */
    void deleteDocument(Long documentId, Long userId);

    /**
     * 获取文档的块列表
     * @param documentId 文档ID
     * @param userId 用户ID
     * @return 块列表
     */
    List<KnowledgeChunkDTO> getDocumentChunks(Long documentId, Long userId);

    /**
     * 检索知识库（根据查询内容检索相关文档块）
     * @param userId 用户ID
     * @param searchDTO 检索DTO
     * @return 检索结果
     */
    KnowledgeSearchResultDTO search(Long userId, KnowledgeSearchDTO searchDTO);

    /**
     * 基于知识库的问答（检索 + LLM生成答案）
     * @param userId 用户ID
     * @param qaDTO 问答DTO
     * @return 问答结果
     */
    KnowledgeQAResponseDTO ask(Long userId, KnowledgeQADTO qaDTO);
}

