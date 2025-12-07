package com.example.demo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "ai-agent")
public class AiAgentConfig {

    /**
     * 后端基地址，例如 http://127.0.0.1:8000
     */
    private String baseUrl = "http://127.0.0.1:8000";

    /**
     * 对话接口路径，例如 /api/chat
     */
    private String chatPath = "/api/chat";

    /**
     * 连接超时时间（毫秒）
     */
    private int connectTimeoutMs = 5000;

    /**
     * 读取超时时间（毫秒），0 表示不超时。
     */
    private int readTimeoutMs = 120000;

    public String getChatUrl() {
        if (chatPath == null || chatPath.isEmpty()) {
            return normalizeBaseUrl(baseUrl);
        }
        if (chatPath.startsWith("http://") || chatPath.startsWith("https://")) {
            return chatPath;
        }
        String sanitizedBase = normalizeBaseUrl(baseUrl);
        String normalizedPath = chatPath.startsWith("/") ? chatPath : "/" + chatPath;
        return sanitizedBase + normalizedPath;
    }

    private String normalizeBaseUrl(String value) {
        if (value == null || value.isEmpty()) {
            return "http://127.0.0.1:8000";
        }
        if (value.endsWith("/")) {
            return value.substring(0, value.length() - 1);
        }
        return value;
    }
}




