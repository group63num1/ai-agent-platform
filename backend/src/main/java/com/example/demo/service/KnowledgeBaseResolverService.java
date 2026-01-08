package com.example.demo.service;

import com.example.demo.app.entity.KnowledgeBase;
import com.example.demo.app.mapper.KnowledgeBaseMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 将 Agent 中保存的“知识库名称列表”解析为 aiagent 需要的 “kb_id 列表”。
 * 不改变数据库中保存的原始名称，仅用于构造发往 aiagent 的请求。
 */
@Service
public class KnowledgeBaseResolverService {

    private static final Long SHARED_KNOWLEDGE_USER_ID = 1L;

    @Autowired
    private KnowledgeBaseMapper knowledgeBaseMapper;

    /**
     * 输入：知识库名称列表（例如 ["RAG1"]）
     * 输出：kb_id 列表（例如 ["12_72447828330623c1"]）
     */
    public List<String> resolveKnowledgeBaseIds(Long userId, List<String> knowledgeBaseNames) {
        if (userId == null || knowledgeBaseNames == null || knowledgeBaseNames.isEmpty()) {
            return Collections.emptyList();
        }

        Set<String> kbIds = new LinkedHashSet<>();
        for (String name : knowledgeBaseNames) {
            if (name == null || name.trim().isEmpty()) {
                continue;
            }
            String trimmed = name.trim();

            KnowledgeBase kb = knowledgeBaseMapper.selectByUserIdAndName(userId, trimmed);
            if (kb == null && !userId.equals(SHARED_KNOWLEDGE_USER_ID)) {
                kb = knowledgeBaseMapper.selectByUserIdAndName(SHARED_KNOWLEDGE_USER_ID, trimmed);
            }
            if (kb == null || kb.getKbId() == null || kb.getKbId().trim().isEmpty()) {
                continue;
            }
            kbIds.add(kb.getKbId().trim());
        }
        return new ArrayList<>(kbIds);
    }
}


