package com.example.demo.service.impl;

import com.example.demo.app.entity.KbDocument;
import com.example.demo.app.entity.KnowledgeBase;
import com.example.demo.app.mapper.KbDocumentMapper;
import com.example.demo.app.mapper.KnowledgeBaseMapper;
import com.example.demo.config.AiAgentConfig;
import com.example.demo.dto.*;
import com.example.demo.service.KnowledgeBaseService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class KnowledgeBaseServiceImpl implements KnowledgeBaseService {

    private static final String DEFAULT_CATEGORY = "personal";
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final String AI_AGENT_KB_PATH = "/api/knowledge-bases";
    private static final String AI_AGENT_KB_QUERY_PATH = "/api/knowledge-bases/query";
    private static final int DEFAULT_CHUNK_SIZE = 1000;
    private static final int DEFAULT_CHUNK_OVERLAP = 200;
    private static final String DEFAULT_CHUNK_METHOD = "recursive";

    @Autowired
    private KnowledgeBaseMapper knowledgeBaseMapper;

    @Autowired
    private KbDocumentMapper kbDocumentMapper;

    @Autowired
    private AiAgentConfig aiAgentConfig;

    @Autowired
    private org.springframework.web.client.RestTemplate restTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public KnowledgeBaseDTO create(Long userId, KnowledgeBaseCreateRequest request) throws IOException {
        String kbId = createOnAiAgent(userId, request);
        KnowledgeBase kb = new KnowledgeBase();
        kb.setKbId(kbId);
        kb.setUserId(userId);
        kb.setName(request.getName());
        kb.setDescription(request.getDescription());
        kb.setCategory(StringUtils.defaultIfBlank(request.getCategory(), DEFAULT_CATEGORY));
        kb.setDocumentCount(0);
        kb.setChunkCount(0);
        kb.setTotalSize(0L);
        kb.setEnabled(true);
        LocalDateTime now = LocalDateTime.now();
        kb.setCreatedAt(now);
        kb.setUpdatedAt(now);
        knowledgeBaseMapper.insert(kb);
        return toDto(kb);
    }

    @Override
    public KnowledgeBaseListResponse list(String keyword, int page, int pageSize) {
        int safePage = page > 0 ? page : DEFAULT_PAGE;
        int safeSize = pageSize > 0 ? pageSize : DEFAULT_PAGE_SIZE;
        int offset = (safePage - 1) * safeSize;
        List<KnowledgeBase> list = knowledgeBaseMapper.selectPage(keyword, offset, safeSize);
        int total = knowledgeBaseMapper.count(keyword);
        KnowledgeBaseListResponse resp = new KnowledgeBaseListResponse();
        resp.setItems(list.stream().map(this::toDto).collect(Collectors.toList()));
        resp.setTotal(total);
        resp.setPage(safePage);
        resp.setPageSize(safeSize);
        return resp;
    }

    @Override
    public KnowledgeBaseDTO get(String kbId) {
        KnowledgeBase kb = requireKb(kbId);
        return toDto(kb);
    }

    @Override
    @Transactional
    public KnowledgeBaseDTO update(String kbId, KnowledgeBaseUpdateRequest request) {
        KnowledgeBase kb = requireKb(kbId);
        if (StringUtils.isNotBlank(request.getName())) {
            kb.setName(request.getName());
        }
        if (request.getDescription() != null) {
            kb.setDescription(request.getDescription());
        }
        if (StringUtils.isNotBlank(request.getCategory())) {
            kb.setCategory(request.getCategory());
        }
        if (request.getEnabled() != null) {
            kb.setEnabled(request.getEnabled());
        }
        kb.setUpdatedAt(LocalDateTime.now());
        knowledgeBaseMapper.update(kb);
        return toDto(kb);
    }

    @Override
    @Transactional
    public void delete(String kbId) {
        KnowledgeBase kb = requireKb(kbId);
        deleteOnAiAgent(kbId);
        kbDocumentMapper.selectByKbId(kbId).forEach(doc -> kbDocumentMapper.deleteById(doc.getId()));
        knowledgeBaseMapper.delete(kb.getKbId());
    }

    @Override
    public List<KbDocumentDTO> listDocuments(String kbId) {
        requireKb(kbId);
        List<KbDocument> docs = kbDocumentMapper.selectByKbId(kbId);
        return docs.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public KbDocumentDTO uploadDocument(String kbId, MultipartFile file, String splitMethod, Integer chunkSize) throws IOException {
        KnowledgeBase kb = requireKb(kbId);
        String filename = Objects.requireNonNull(file.getOriginalFilename());
        byte[] bytes = file.getBytes();
        callAiAgentUpdate(kb.getKbId(), filename, new String(bytes, StandardCharsets.UTF_8), splitMethod, chunkSize);

        KbDocument doc = new KbDocument();
        doc.setKbId(kbId);
        doc.setName(filename);
        doc.setSize((long) bytes.length);
        doc.setChunkCount(null);
        doc.setStatus("processed");
        doc.setSplitMethod(StringUtils.defaultIfBlank(splitMethod, DEFAULT_CHUNK_METHOD));
        doc.setChunkSize(chunkSize != null ? chunkSize : DEFAULT_CHUNK_SIZE);
        doc.setUploadedAt(LocalDateTime.now());
        kbDocumentMapper.insert(doc);

        kb.setDocumentCount((kb.getDocumentCount() == null ? 0 : kb.getDocumentCount()) + 1);
        kb.setTotalSize((kb.getTotalSize() == null ? 0L : kb.getTotalSize()) + doc.getSize());
        kb.setUpdatedAt(LocalDateTime.now());
        knowledgeBaseMapper.update(kb);
        return toDto(doc);
    }

    @Override
    @Transactional
    public void deleteDocument(String kbId, Long docId) {
        requireKb(kbId);
        kbDocumentMapper.deleteById(docId);
    }

    @Override
    public KnowledgeBaseSearchResponse search(String kbId, KnowledgeBaseSearchRequest request) {
        KnowledgeBase kb = requireKb(kbId);
        Map<String, Object> payload = new HashMap<>();
        payload.put("query_text", request.getQuery());
        payload.put("similarity_threshold", request.getSimilarityThreshold() == null ? 0.0 : request.getSimilarityThreshold());
        payload.put("limit", request.getTopK() == null ? 20 : request.getTopK());
        payload.put("user_id", kb.getUserId() == null ? "" : String.valueOf(kb.getUserId()));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
        ResponseEntity<String> resp = restTemplate.postForEntity(buildUrl(AI_AGENT_KB_QUERY_PATH), entity, String.class);
        if (!resp.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("AI Agent 知识库检索失败: " + resp.getStatusCodeValue());
        }
        return parseSearchResponse(resp.getBody(), request);
    }

    private KnowledgeBaseSearchResponse parseSearchResponse(String body, KnowledgeBaseSearchRequest request) {
        KnowledgeBaseSearchResponse response = new KnowledgeBaseSearchResponse();
        response.setQuery(request.getQuery());
        response.setTopK(request.getTopK());
        response.setSimilarityThreshold(request.getSimilarityThreshold());
        try {
            JsonNode root = objectMapper.readTree(body);
            JsonNode results = root.path("results");
            if (!results.isArray()) {
                response.setItems(Collections.emptyList());
                response.setTotal(0);
                return response;
            }
            List<SearchResultItemDTO> items = new ArrayList<>();
            for (JsonNode node : results) {
                SearchResultItemDTO item = new SearchResultItemDTO();
                item.setId(node.path("id").asText(null));
                item.setContent(node.path("text").asText(""));
                item.setSource(node.path("source").asText(""));
                item.setScore(node.path("similarity").isMissingNode() ? null : node.path("similarity").asDouble());
                item.setChunkIndex(node.path("chunkIndex").isMissingNode() ? null : node.path("chunkIndex").asInt());
                items.add(item);
            }
            response.setItems(items);
            response.setTotal(items.size());
            return response;
        } catch (IOException e) {
            throw new RuntimeException("解析 AI Agent 检索响应失败", e);
        }
    }

    private String createOnAiAgent(Long userId, KnowledgeBaseCreateRequest request) throws IOException {
        Map<String, Object> payload = new HashMap<>();
        payload.put("user_id", userId == null ? "" : String.valueOf(userId));
        payload.put("name", request.getName());
        payload.put("description", request.getDescription());
        payload.put("files", new ArrayList<>());
        payload.put("chunk_size", DEFAULT_CHUNK_SIZE);
        payload.put("chunk_overlap", DEFAULT_CHUNK_OVERLAP);
        payload.put("chunking_method", DEFAULT_CHUNK_METHOD);
        payload.put("enabled", true);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
        ResponseEntity<String> resp = restTemplate.postForEntity(buildUrl(AI_AGENT_KB_PATH), entity, String.class);
        if (!resp.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("AI Agent 创建知识库失败: " + resp.getStatusCodeValue());
        }
        JsonNode root = objectMapper.readTree(resp.getBody());
        String kbId = root.path("kb_id").asText(null);
        if (StringUtils.isBlank(kbId)) {
            throw new RuntimeException("AI Agent 未返回知识库ID");
        }
        return kbId;
    }

    private void deleteOnAiAgent(String kbId) {
        String url = buildUrl(AI_AGENT_KB_PATH + "/" + kbId);
        try {
            restTemplate.delete(url);
        } catch (Exception e) {
            throw new RuntimeException("AI Agent 删除知识库失败: " + e.getMessage(), e);
        }
    }

    private void callAiAgentUpdate(String kbId, String filename, String content, String splitMethod, Integer chunkSize) {
        Map<String, Object> fileObj = new HashMap<>();
        fileObj.put("filename", filename);
        fileObj.put("content", content);
        Map<String, Object> payload = new HashMap<>();
        payload.put("files", Collections.singletonList(fileObj));
        payload.put("chunk_size", chunkSize != null ? chunkSize : DEFAULT_CHUNK_SIZE);
        payload.put("chunk_overlap", DEFAULT_CHUNK_OVERLAP);
        payload.put("chunking_method", StringUtils.defaultIfBlank(splitMethod, DEFAULT_CHUNK_METHOD));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
        ResponseEntity<String> resp = restTemplate.exchange(
                buildUrl(AI_AGENT_KB_PATH + "/" + kbId),
                HttpMethod.PUT,
                entity,
                String.class);
        if (!resp.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("AI Agent 更新知识库失败: " + resp.getStatusCodeValue());
        }
    }

    private KnowledgeBase requireKb(String kbId) {
        KnowledgeBase kb = knowledgeBaseMapper.selectById(kbId);
        if (kb == null) {
            throw new RuntimeException("知识库不存在");
        }
        return kb;
    }

    private KnowledgeBaseDTO toDto(KnowledgeBase kb) {
        KnowledgeBaseDTO dto = new KnowledgeBaseDTO();
        dto.setKbId(kb.getKbId());
        dto.setName(kb.getName());
        dto.setDescription(kb.getDescription());
        dto.setCategory(kb.getCategory());
        dto.setDocumentCount(kb.getDocumentCount());
        dto.setChunkCount(kb.getChunkCount());
        dto.setTotalSize(kb.getTotalSize());
        dto.setMilvusCollection(kb.getMilvusCollection());
        dto.setEnabled(kb.getEnabled());
        dto.setCreatedAt(kb.getCreatedAt());
        dto.setUpdatedAt(kb.getUpdatedAt());
        return dto;
    }

    private KbDocumentDTO toDto(KbDocument doc) {
        KbDocumentDTO dto = new KbDocumentDTO();
        dto.setId(doc.getId());
        dto.setKbId(doc.getKbId());
        dto.setName(doc.getName());
        dto.setSize(doc.getSize());
        dto.setChunkCount(doc.getChunkCount());
        dto.setStatus(doc.getStatus());
        dto.setSplitMethod(doc.getSplitMethod());
        dto.setChunkSize(doc.getChunkSize());
        dto.setUploadedAt(doc.getUploadedAt());
        return dto;
    }

    private String buildUrl(String path) {
        String base = Optional.ofNullable(aiAgentConfig.getBaseUrl()).orElse("http://127.0.0.1:8000");
        String normalizedBase = base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
        String normalizedPath = path.startsWith("/") ? path : "/" + path;
        return normalizedBase + normalizedPath;
    }
}

