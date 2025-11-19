// src/main/java/com/example/demo/service/impl/OpenAILlmServiceImpl.java
package com.example.demo.service.impl;

import com.example.demo.config.LlmConfig;
import com.example.demo.service.LlmService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OpenAILlmServiceImpl implements LlmService {

    private static final Logger logger = LoggerFactory.getLogger(OpenAILlmServiceImpl.class);

    @Autowired
    private LlmConfig llmConfig;

    @Autowired
    private RestTemplate restTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String generateResponse(List<Map<String, String>> messages) {
        // 如果没有配置API密钥，返回默认回复
        if (llmConfig.getApiKey() == null || llmConfig.getApiKey().trim().isEmpty()) {
            logger.warn("LLM API密钥未配置，返回默认回复");
            return "抱歉，AI服务暂未配置。请配置LLM API密钥后使用。";
        }

        try {
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", llmConfig.getModel());
            requestBody.put("messages", messages);
            requestBody.put("max_tokens", llmConfig.getMaxTokens());
            requestBody.put("temperature", llmConfig.getTemperature());

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String apiKey = llmConfig.getApiKey();
            if (apiKey != null && !apiKey.trim().isEmpty()) {
                headers.setBearerAuth(apiKey.trim());
            } else {
                throw new RuntimeException("LLM API密钥未配置");
            }

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            // 发送请求
            String apiUrl = llmConfig.getApiUrl();
            if (apiUrl == null || apiUrl.trim().isEmpty()) {
                throw new RuntimeException("LLM API URL未配置");
            }
            logger.info("调用LLM API: {}", apiUrl);
            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl.trim(),
                    HttpMethod.POST,
                    request,
                    String.class
            );

            // 解析响应
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                JsonNode choices = jsonNode.get("choices");
                if (choices != null && choices.isArray() && choices.size() > 0) {
                    JsonNode message = choices.get(0).get("message");
                    if (message != null && message.has("content")) {
                        String content = message.get("content").asText();
                        logger.info("LLM API调用成功，返回内容长度: {}", content.length());
                        return content;
                    }
                }
                throw new RuntimeException("LLM API返回格式异常");
            } else {
                throw new RuntimeException("LLM API调用失败: " + response.getStatusCode());
            }

        } catch (org.springframework.web.client.ResourceAccessException e) {
            logger.error("LLM API调用超时或网络错误", e);
            throw new RuntimeException("AI服务连接超时，请稍后重试");
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            logger.error("LLM API调用失败: {}", e.getResponseBodyAsString(), e);
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new RuntimeException("AI服务认证失败，请检查API密钥配置");
            } else if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                throw new RuntimeException("AI服务请求参数错误: " + e.getResponseBodyAsString());
            } else {
                throw new RuntimeException("AI服务调用失败: " + e.getMessage());
            }
        } catch (Exception e) {
            logger.error("LLM API调用异常", e);
            throw new RuntimeException("AI服务调用异常: " + e.getMessage());
        }
    }
}

