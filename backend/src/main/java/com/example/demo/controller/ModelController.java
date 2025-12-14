package com.example.demo.controller;

import com.example.demo.common.ApiResponse;
import com.example.demo.config.AiAgentConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/models")
public class ModelController {

    private static final String AI_AGENT_MODELS_PATH = "/api/models";

    @Autowired
    private AiAgentConfig aiAgentConfig;

    @Autowired
    private org.springframework.web.client.RestTemplate restTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping
    public ApiResponse<Object> listModels() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_JSON));
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> resp = restTemplate.exchange(
                    buildUrl(AI_AGENT_MODELS_PATH),
                    HttpMethod.GET,
                    entity,
                    String.class);

            if (!resp.getStatusCode().is2xxSuccessful()) {
                return ApiResponse.fail(400, "获取模型列表失败: " + resp.getStatusCodeValue());
            }

            String body = resp.getBody();
            if (body == null || body.trim().isEmpty()) {
                return ApiResponse.ok(java.util.Collections.emptyMap());
            }

            Object data = objectMapper.readValue(body, Object.class);
            return ApiResponse.ok(data);
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    private String buildUrl(String path) {
        String base = Optional.ofNullable(aiAgentConfig.getBaseUrl()).orElse("http://127.0.0.1:8000");
        String normalizedBase = base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
        String normalizedPath = path.startsWith("/") ? path : "/" + path;
        return normalizedBase + normalizedPath;
    }
}
