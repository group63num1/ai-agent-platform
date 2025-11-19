// src/main/java/com/example/demo/config/LlmConfig.java
package com.example.demo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
@ConfigurationProperties(prefix = "llm")
@Data
public class LlmConfig {

    private String provider = "tencent"; // 提供商：tencent 或 openai
    private String apiKey;
    private String apiUrl = "https://hunyuan.tencentcloudapi.com";
    private String model = "hunyuan-lite";
    private Integer maxTokens = 1000;
    private Double temperature = 0.7;
    private Integer timeout = 30000; // 30秒超时
    
    // 聊天上下文配置
    @Data
    public static class ChatConfig {
        private String systemPrompt = "你是一个专业的智能客服助手，能够准确理解用户的问题并提供有帮助的回答。请用友好、专业、简洁的方式回答问题。如果用户提到之前对话的内容，请根据上下文进行回答。";
        private Integer maxHistory = 30; // 最大历史消息数量
    }
    
    private ChatConfig chat = new ChatConfig();

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeout);
        factory.setReadTimeout(timeout);
        return new RestTemplate(factory);
    }
}

