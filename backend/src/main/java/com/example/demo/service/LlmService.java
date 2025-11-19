// src/main/java/com/example/demo/service/LlmService.java
package com.example.demo.service;

import java.util.List;
import java.util.Map;

public interface LlmService {
    /**
     * 生成AI回复
     * @param messages 对话消息列表，格式：[{"role": "user", "content": "..."}, ...]
     * @return AI回复内容
     */
    String generateResponse(List<Map<String, String>> messages);
}

