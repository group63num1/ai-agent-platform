// src/main/java/com/example/demo/service/impl/KnowledgeServiceImpl.java
package com.example.demo.service.impl;

import com.example.demo.app.entity.KnowledgeDocument;
import com.example.demo.app.entity.KnowledgeChunk;
import com.example.demo.app.mapper.KnowledgeDocumentMapper;
import com.example.demo.app.mapper.KnowledgeChunkMapper;
import com.example.demo.dto.*;
import com.example.demo.service.KnowledgeService;
import com.example.demo.service.LlmService;
import com.example.demo.util.BeanConvertUtil;
import com.example.demo.util.TextChunkUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class KnowledgeServiceImpl implements KnowledgeService {

    @Autowired
    private KnowledgeDocumentMapper documentMapper;

    @Autowired
    private KnowledgeChunkMapper chunkMapper;

    @Autowired
    private LlmService llmService;

    @Override
    @Transactional
    public KnowledgeDocumentDTO createDocument(Long userId, KnowledgeDocumentCreateDTO createDTO) {
        // 1. 计算内容哈希值（用于去重）
        String contentHash = calculateHash(createDTO.getContent());

        // 2. 创建文档记录
        KnowledgeDocument document = new KnowledgeDocument();
        document.setUserId(userId);
        document.setTitle(createDTO.getTitle());
        document.setFileName(createDTO.getFileName());
        document.setFileType(createDTO.getFileType());
        document.setFileSize(createDTO.getFileSize());
        document.setFilePath(createDTO.getFilePath());
        document.setContent(createDTO.getContent());
        document.setContentHash(contentHash);
        document.setStatus(0); // 0-处理中
        document.setChunkCount(0);
        document.setIsDeleted(0);
        document.setCreatedAt(LocalDateTime.now());
        document.setUpdatedAt(LocalDateTime.now());

        // 3. 保存文档
        int result = documentMapper.insert(document);
        if (result <= 0) {
            throw new RuntimeException("创建文档失败");
        }

        try {
            // 4. 分片处理
            List<String> chunks = TextChunkUtil.chunkText(createDTO.getContent());
            
            if (chunks.isEmpty()) {
                throw new RuntimeException("文档分片失败：内容为空");
            }

            // 5. 保存文档块
            List<KnowledgeChunk> chunkList = new ArrayList<>();
            for (int i = 0; i < chunks.size(); i++) {
                String chunkContent = chunks.get(i);
                KnowledgeChunk chunk = new KnowledgeChunk();
                chunk.setDocumentId(document.getId());
                chunk.setChunkIndex(i);
                chunk.setContent(chunkContent);
                chunk.setContentHash(calculateHash(chunkContent));
                chunk.setTokenCount(TextChunkUtil.estimateTokenCount(chunkContent));
                chunk.setCharCount(chunkContent.length());
                chunk.setCreatedAt(LocalDateTime.now());
                chunkList.add(chunk);
            }

            // 批量插入文档块
            if (!chunkList.isEmpty()) {
                chunkMapper.insertBatch(chunkList);
            }

            // 6. 更新文档状态和块数量
            document.setStatus(1); // 1-已完成
            document.setChunkCount(chunkList.size());
            documentMapper.update(document);

            // 7. 返回DTO
            return BeanConvertUtil.convert(document, KnowledgeDocumentDTO.class);

        } catch (Exception e) {
            // 处理失败，更新文档状态
            documentMapper.updateStatus(document.getId(), 2, e.getMessage());
            throw new RuntimeException("文档分片处理失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<KnowledgeDocumentDTO> getUserDocuments(Long userId) {
        // 返回所有可访问的文档：包括共享知识库（user_id = 1）和当前用户的文档
        Long SHARED_KNOWLEDGE_USER_ID = 1L; // 系统共享知识库用户ID
        List<KnowledgeDocument> documents = new ArrayList<>();
        
        // 添加共享知识库文档（所有用户都可以访问）
        List<KnowledgeDocument> sharedDocs = documentMapper.selectByUserId(SHARED_KNOWLEDGE_USER_ID, false);
        documents.addAll(sharedDocs);
        
        // 添加当前用户的文档（如果当前用户不是共享知识库用户）
        if (!userId.equals(SHARED_KNOWLEDGE_USER_ID)) {
            List<KnowledgeDocument> userDocs = documentMapper.selectByUserId(userId, false);
            documents.addAll(userDocs);
        }
        
        return BeanConvertUtil.convertList(documents, KnowledgeDocumentDTO.class);
    }

    @Override
    public KnowledgeDocumentDTO getDocumentById(Long documentId, Long userId) {
        KnowledgeDocument document = documentMapper.selectById(documentId);
        if (document == null) {
            throw new RuntimeException("文档不存在");
        }

        // 验证文档权限：共享知识库（user_id = 1）或当前用户的文档都可以访问
        Long SHARED_KNOWLEDGE_USER_ID = 1L; // 系统共享知识库用户ID
        if (!document.getUserId().equals(userId) && !document.getUserId().equals(SHARED_KNOWLEDGE_USER_ID)) {
            throw new RuntimeException("无权访问该文档");
        }

        return BeanConvertUtil.convert(document, KnowledgeDocumentDTO.class);
    }

    @Override
    @Transactional
    public void deleteDocument(Long documentId, Long userId) {
        KnowledgeDocument document = documentMapper.selectById(documentId);
        if (document == null) {
            throw new RuntimeException("文档不存在");
        }

        Long SHARED_KNOWLEDGE_USER_ID = 1L; // 系统共享知识库用户ID
        
        // 验证文档权限：只有文档所有者可以删除（共享知识库文档不能删除）
        if (!document.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除该文档");
        }
        
        // 防止删除共享知识库文档（除非是系统管理员，这里简化处理）
        if (document.getUserId().equals(SHARED_KNOWLEDGE_USER_ID) && !userId.equals(SHARED_KNOWLEDGE_USER_ID)) {
            throw new RuntimeException("无法删除共享知识库文档");
        }

        int result = documentMapper.softDelete(documentId);
        if (result <= 0) {
            throw new RuntimeException("删除文档失败");
        }
    }

    @Override
    public List<KnowledgeChunkDTO> getDocumentChunks(Long documentId, Long userId) {
        // 验证文档权限
        getDocumentById(documentId, userId);

        List<KnowledgeChunk> chunks = chunkMapper.selectByDocumentId(documentId);
        return BeanConvertUtil.convertList(chunks, KnowledgeChunkDTO.class);
    }

    @Override
    public KnowledgeSearchResultDTO search(Long userId, KnowledgeSearchDTO searchDTO) {
        // 1. 获取可搜索的文档（包括共享知识库和用户自己的文档）
        Long SHARED_KNOWLEDGE_USER_ID = 1L; // 系统共享知识库用户ID
        List<KnowledgeDocument> documents;
        
        if (searchDTO.getDocumentId() != null) {
            // 指定文档
            KnowledgeDocument document = documentMapper.selectById(searchDTO.getDocumentId());
            if (document == null) {
                throw new RuntimeException("文档不存在");
            }
            // 验证权限：共享知识库或当前用户的文档都可以访问
            if (!document.getUserId().equals(userId) && !document.getUserId().equals(SHARED_KNOWLEDGE_USER_ID)) {
                throw new RuntimeException("无权访问该文档");
            }
            documents = Collections.singletonList(document);
        } else {
            // 搜索范围：包括共享知识库（user_id = 1）和当前用户的文档
            documents = new ArrayList<>();
            
            // 添加共享知识库文档（所有用户都可以访问）
            List<KnowledgeDocument> sharedDocs = documentMapper.selectByUserId(SHARED_KNOWLEDGE_USER_ID, false);
            documents.addAll(sharedDocs);
            
            // 添加当前用户的文档（如果当前用户不是共享知识库用户）
            if (!userId.equals(SHARED_KNOWLEDGE_USER_ID)) {
                List<KnowledgeDocument> userDocs = documentMapper.selectByUserId(userId, false);
                documents.addAll(userDocs);
            }
        }

        // 2. 从所有文档中检索相关块
        List<KnowledgeChunk> allChunks = new ArrayList<>();
        for (KnowledgeDocument doc : documents) {
            if (doc.getStatus() == 1) { // 只检索已完成的文档
                List<KnowledgeChunk> chunks = chunkMapper.selectByDocumentId(doc.getId());
                allChunks.addAll(chunks);
            }
        }

        // 3. 简单的文本匹配检索（后续可以优化为向量检索）
        List<KnowledgeChunk> matchedChunks = simpleTextSearch(allChunks, searchDTO.getQuery(), searchDTO.getTopK());

        // 4. 构建结果
        KnowledgeSearchResultDTO result = new KnowledgeSearchResultDTO();
        result.setChunks(BeanConvertUtil.convertList(matchedChunks, KnowledgeChunkDTO.class));
        result.setTotalCount(matchedChunks.size());
        result.setQuery(searchDTO.getQuery());

        return result;
    }

    @Override
    public KnowledgeQAResponseDTO ask(Long userId, KnowledgeQADTO qaDTO) {
        // 1. 检索相关文档块
        KnowledgeSearchDTO searchDTO = new KnowledgeSearchDTO();
        searchDTO.setQuery(qaDTO.getQuestion());
        searchDTO.setTopK(qaDTO.getTopK());
        searchDTO.setDocumentId(qaDTO.getDocumentId());

        KnowledgeSearchResultDTO searchResult = search(userId, searchDTO);

        // 2. 构建上下文
        List<Map<String, String>> messages = new ArrayList<>();

        // 2.1 系统提示词
        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", "你是一个专业的智能客服助手，能够基于提供的知识库内容回答用户的问题。请根据以下知识库内容回答问题，如果知识库中没有相关信息，请说明无法回答。");
        messages.add(systemMessage);

        // 2.2 构建知识库上下文
        if (searchResult.getChunks() != null && !searchResult.getChunks().isEmpty()) {
            StringBuilder context = new StringBuilder();
            context.append("知识库内容：\n\n");
            
            for (int i = 0; i < searchResult.getChunks().size(); i++) {
                KnowledgeChunkDTO chunk = searchResult.getChunks().get(i);
                context.append("【片段").append(i + 1).append("】\n");
                context.append(chunk.getContent()).append("\n\n");
            }

            Map<String, String> contextMessage = new HashMap<>();
            contextMessage.put("role", "system");
            contextMessage.put("content", context.toString());
            messages.add(contextMessage);
        } else {
            // 如果没有检索到相关内容，添加提示
            Map<String, String> noContextMessage = new HashMap<>();
            noContextMessage.put("role", "system");
            noContextMessage.put("content", "知识库中没有找到与问题相关的信息。");
            messages.add(noContextMessage);
        }

        // 2.3 用户问题
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", qaDTO.getQuestion());
        messages.add(userMessage);

        // 3. 调用LLM生成答案
        String answer = llmService.generateResponse(messages);

        // 4. 构建响应
        KnowledgeQAResponseDTO response = new KnowledgeQAResponseDTO();
        response.setQuestion(qaDTO.getQuestion());
        response.setAnswer(answer);
        response.setSources(searchResult.getChunks());
        response.setSourceCount(searchResult.getChunks() != null ? searchResult.getChunks().size() : 0);

        return response;
    }

    /**
     * 简单的文本匹配检索
     * 后续可以优化为向量相似度检索
     */
    private List<KnowledgeChunk> simpleTextSearch(List<KnowledgeChunk> chunks, String query, int topK) {
        if (chunks.isEmpty() || query == null || query.trim().isEmpty()) {
            return new ArrayList<>();
        }

        // 简单的关键词匹配评分
        List<ScoredChunk> scoredChunks = new ArrayList<>();
        String queryLower = query.toLowerCase();

        for (KnowledgeChunk chunk : chunks) {
            String contentLower = chunk.getContent().toLowerCase();
            int score = 0;

            // 计算关键词出现次数
            String[] queryWords = queryLower.split("\\s+");
            for (String word : queryWords) {
                if (word.length() > 1) { // 忽略单字符
                    int count = countOccurrences(contentLower, word);
                    score += count * 10; // 每个匹配词10分
                }
            }

            // 完全匹配加分
            if (contentLower.contains(queryLower)) {
                score += 100;
            }

            if (score > 0) {
                scoredChunks.add(new ScoredChunk(chunk, score));
            }
        }

        // 按分数排序，取前topK个
        return scoredChunks.stream()
                .sorted((a, b) -> Integer.compare(b.score, a.score))
                .limit(topK)
                .map(sc -> sc.chunk)
                .collect(Collectors.toList());
    }

    /**
     * 计算字符串出现次数
     */
    private int countOccurrences(String text, String pattern) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(pattern, index)) != -1) {
            count++;
            index += pattern.length();
        }
        return count;
    }

    /**
     * 带评分的块
     */
    private static class ScoredChunk {
        KnowledgeChunk chunk;
        int score;

        ScoredChunk(KnowledgeChunk chunk, int score) {
            this.chunk = chunk;
            this.score = score;
        }
    }

    /**
     * 计算内容的SHA-256哈希值
     */
    private String calculateHash(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("计算哈希值失败", e);
        }
    }
}

