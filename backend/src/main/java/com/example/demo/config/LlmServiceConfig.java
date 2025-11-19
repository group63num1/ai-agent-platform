// src/main/java/com/example/demo/config/LlmServiceConfig.java
package com.example.demo.config;

import com.example.demo.service.LlmService;
import com.example.demo.service.impl.OpenAILlmServiceImpl;
import com.example.demo.service.impl.TencentLlmServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class LlmServiceConfig {

    @Autowired
    private LlmConfig llmConfig;

    @Autowired
    private org.springframework.web.client.RestTemplate restTemplate;

    @Bean
    @Primary
    public LlmService llmService() {
        String provider = llmConfig.getProvider();
        if (provider == null || provider.trim().isEmpty()) {
            provider = "tencent"; // 默认使用腾讯云
        }
        
        provider = provider.toLowerCase().trim();
        
        if ("tencent".equals(provider)) {
            TencentLlmServiceImpl service = new TencentLlmServiceImpl();
            // 使用反射或setter注入依赖
            try {
                java.lang.reflect.Field configField = TencentLlmServiceImpl.class.getDeclaredField("llmConfig");
                configField.setAccessible(true);
                configField.set(service, llmConfig);
                
                java.lang.reflect.Field restTemplateField = TencentLlmServiceImpl.class.getDeclaredField("restTemplate");
                restTemplateField.setAccessible(true);
                restTemplateField.set(service, restTemplate);
            } catch (Exception e) {
                throw new RuntimeException("初始化TencentLlmService失败", e);
            }
            return service;
        } else if ("openai".equals(provider)) {
            OpenAILlmServiceImpl service = new OpenAILlmServiceImpl();
            try {
                java.lang.reflect.Field configField = OpenAILlmServiceImpl.class.getDeclaredField("llmConfig");
                configField.setAccessible(true);
                configField.set(service, llmConfig);
                
                java.lang.reflect.Field restTemplateField = OpenAILlmServiceImpl.class.getDeclaredField("restTemplate");
                restTemplateField.setAccessible(true);
                restTemplateField.set(service, restTemplate);
            } catch (Exception e) {
                throw new RuntimeException("初始化OpenAILlmService失败", e);
            }
            return service;
        } else {
            // 默认使用腾讯云
            TencentLlmServiceImpl service = new TencentLlmServiceImpl();
            try {
                java.lang.reflect.Field configField = TencentLlmServiceImpl.class.getDeclaredField("llmConfig");
                configField.setAccessible(true);
                configField.set(service, llmConfig);
                
                java.lang.reflect.Field restTemplateField = TencentLlmServiceImpl.class.getDeclaredField("restTemplate");
                restTemplateField.setAccessible(true);
                restTemplateField.set(service, restTemplate);
            } catch (Exception e) {
                throw new RuntimeException("初始化TencentLlmService失败", e);
            }
            return service;
        }
    }
}

