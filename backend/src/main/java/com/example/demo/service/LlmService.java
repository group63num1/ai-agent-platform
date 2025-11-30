package com.example.demo.service;

import java.util.List;
import java.util.Map;

/**
 * LLM 统一适配接口，便于在不同实现之间切换。
 */
public interface LlmService {

    /**
     * 根据消息列表生成模型回复。
     *
     * @param messages 消息序列，需包含 role/content 字段
     * @return 模型回复内容
     */
    String generateResponse(List<Map<String, String>> messages);

    /**
     * 根据指定模型生成回复，默认回退为全局模型。
     */
    default String generateResponse(List<Map<String, String>> messages, String modelName) {
        return generateResponse(messages);
    }
}


