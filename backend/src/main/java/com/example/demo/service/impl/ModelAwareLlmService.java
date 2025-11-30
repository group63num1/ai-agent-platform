package com.example.demo.service.impl;

import com.example.demo.config.LlmConfig;
import com.example.demo.service.LlmService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Primary
public class ModelAwareLlmService implements LlmService {

    private static final Logger logger = LoggerFactory.getLogger(ModelAwareLlmService.class);

    private final LlmConfig llmConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final Map<String, String> MODEL_PROVIDER_MAP;

    static {
        Map<String, String> mapping = new HashMap<>();
        mapping.put("千问", "qianwen");
        mapping.put("qwen", "qianwen");
        mapping.put("qianwen", "qianwen");
        mapping.put("豆包", "doubao");
        mapping.put("doubao", "doubao");
        mapping.put("deepseek", "deepseek");
        mapping.put("deepseek-chat", "deepseek");
        mapping.put("混元", "hunyuan");
        mapping.put("hunyuan", "hunyuan");
        mapping.put("tencent", "hunyuan");
        MODEL_PROVIDER_MAP = Collections.unmodifiableMap(mapping);
    }

    public ModelAwareLlmService(LlmConfig llmConfig, RestTemplate restTemplate) {
        this.llmConfig = llmConfig;
        this.restTemplate = restTemplate;
    }

    @Override
    public String generateResponse(List<Map<String, String>> messages) {
        return generateResponse(messages, null);
    }

    @Override
    public String generateResponse(List<Map<String, String>> messages, String modelName) {
        String providerKey = resolveProviderKey(modelName);
        if (isHunyuan(providerKey)) {
            return callHunyuan(messages, modelName);
        }
        return callOpenAiStyle(messages, providerKey, modelName);
    }

    private String callOpenAiStyle(List<Map<String, String>> messages,
                                   String providerKey,
                                   String overrideModel) {
        LlmConfig.ProviderConfig provider = llmConfig.getProviderConfig(providerKey);
        if (provider == null || isBlank(provider.getApiKey())) {
            logger.warn("LLM 提供商 [{}] 未配置，返回默认回复", providerKey);
            return "抱歉，AI服务暂未配置。请配置LLM API密钥后使用。";
        }

        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", firstNonBlank(overrideModel, provider.getModel(), llmConfig.getModel()));
            requestBody.put("messages", messages);
            requestBody.put("max_tokens", firstNonNull(provider.getMaxTokens(), llmConfig.getMaxTokens(), 1024));
            requestBody.put("temperature", firstNonNull(provider.getTemperature(), llmConfig.getTemperature(), 0.7));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(provider.getApiKey().trim());

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            String apiUrl = firstNonBlank(provider.getApiUrl(), llmConfig.getApiUrl());
            logger.info("调用 LLM provider [{}] 接口: {}", providerKey, apiUrl);
            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                JsonNode choices = jsonNode.get("choices");
                if (choices != null && choices.isArray() && choices.size() > 0) {
                    JsonNode message = choices.get(0).get("message");
                    if (message != null && message.has("content")) {
                        String content = message.get("content").asText();
                        logger.debug("LLM [{}] 返回长度: {}", providerKey, content.length());
                        return content;
                    }
                }
                throw new RuntimeException("LLM API 返回格式异常");
            }
            throw new RuntimeException("LLM API调用失败: " + response.getStatusCode());
        } catch (ResourceAccessException e) {
            logger.error("LLM [{}] 调用超时或网络错误", providerKey, e);
            throw new RuntimeException("AI服务连接超时，请稍后重试");
        } catch (HttpClientErrorException e) {
            logger.error("LLM [{}] 调用失败: {}", providerKey, e.getResponseBodyAsString(), e);
            throw new RuntimeException("AI服务调用失败: " + e.getMessage());
        } catch (Exception e) {
            logger.error("LLM [{}] 调用异常", providerKey, e);
            throw new RuntimeException("AI服务调用异常: " + e.getMessage());
        }
    }

    private String callHunyuan(List<Map<String, String>> messages, String overrideModel) {
        LlmConfig.ProviderConfig provider = llmConfig.getProviderConfig("hunyuan");
        if (provider == null || isBlank(provider.getApiKey())) {
            logger.warn("腾讯混元密钥未配置");
            return "抱歉，AI服务暂未配置。请配置LLM API密钥后使用。";
        }

        try {
            String[] credentials = provider.getApiKey().split(":", 2);
            if (credentials.length != 2) {
                throw new RuntimeException("腾讯云API密钥格式错误，应为 SecretId:SecretKey");
            }
            String secretId = credentials[0].trim();
            String secretKey = credentials[1].trim();

            Map<String, Object> requestBody = new HashMap<>();
            String model = firstNonBlank(overrideModel, provider.getModel(), llmConfig.getModel(), "hunyuan-lite");
            requestBody.put("Model", model);

            List<Map<String, Object>> tencentMessages = new ArrayList<>();
            for (Map<String, String> msg : messages) {
                Map<String, Object> tencentMsg = new HashMap<>();
                tencentMsg.put("Role", msg.get("role"));
                tencentMsg.put("Content", msg.get("content"));
                tencentMessages.add(tencentMsg);
            }
            requestBody.put("Messages", tencentMessages);

            String apiUrl = firstNonBlank(provider.getApiUrl(), llmConfig.getApiUrl(), "https://hunyuan.tencentcloudapi.com");
            String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
            String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-TC-Action", "ChatCompletions");
            headers.set("X-TC-Version", "2023-09-01");
            headers.set("X-TC-Timestamp", timestamp);
            headers.set("X-TC-Region", "ap-beijing");

            String authorization = generateTencentSignature(
                    secretId, secretKey, "POST", apiUrl,
                    requestBody, timestamp, date);
            headers.set("Authorization", authorization);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            logger.info("调用腾讯混元API: {}", apiUrl);
            ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, request, String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                if (jsonNode.has("Response") && jsonNode.get("Response").has("Error")) {
                    JsonNode error = jsonNode.get("Response").get("Error");
                    String errorMsg = error.has("Message") ? error.get("Message").asText() : "未知错误";
                    throw new RuntimeException("腾讯云API调用失败: " + errorMsg);
                }
                JsonNode responseNode = jsonNode.get("Response");
                if (responseNode != null && responseNode.has("Choices") && responseNode.get("Choices").isArray()) {
                    JsonNode choices = responseNode.get("Choices");
                    if (choices.size() > 0) {
                        JsonNode choice = choices.get(0);
                        if (choice.has("Message") && choice.get("Message").has("Content")) {
                            return choice.get("Message").get("Content").asText();
                        }
                    }
                }
                throw new RuntimeException("腾讯云API返回格式异常");
            }
            throw new RuntimeException("腾讯云API调用失败: " + response.getStatusCode());
        } catch (ResourceAccessException e) {
            logger.error("腾讯云API调用超时或网络错误", e);
            throw new RuntimeException("AI服务连接超时，请稍后重试");
        } catch (HttpClientErrorException e) {
            logger.error("腾讯云API调用失败: {}", e.getResponseBodyAsString(), e);
            throw new RuntimeException("AI服务调用失败: " + e.getMessage() + " - " + e.getResponseBodyAsString());
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            logger.error("腾讯云API调用异常", e);
            throw new RuntimeException("AI服务调用异常: " + e.getMessage(), e);
        }
    }

    private String generateTencentSignature(String secretId, String secretKey,
                                            String method, String url,
                                            Map<String, Object> requestBody,
                                            String timestamp, String date) {
        try {
            String service = "hunyuan";
            String canonicalUri = "/";
            String canonicalQueryString = "";
            String canonicalHeaders = "content-type:application/json\nhost:" + getHostFromUrl(url) + "\n";
            String signedHeaders = "content-type;host";

            String payload = objectMapper.writeValueAsString(requestBody);
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String hashedRequestPayload = bytesToHex(md.digest(payload.getBytes(StandardCharsets.UTF_8))).toLowerCase();

            String canonicalRequest = method + "\n" +
                    canonicalUri + "\n" +
                    canonicalQueryString + "\n" +
                    canonicalHeaders + "\n" +
                    signedHeaders + "\n" +
                    hashedRequestPayload;

            String algorithm = "TC3-HMAC-SHA256";
            String credentialScope = date + "/" + service + "/tc3_request";
            String hashedCanonicalRequest = bytesToHex(md.digest(canonicalRequest.getBytes(StandardCharsets.UTF_8))).toLowerCase();
            String stringToSign = algorithm + "\n" +
                    timestamp + "\n" +
                    credentialScope + "\n" +
                    hashedCanonicalRequest;

            byte[] secretDate = hmacSha256(("TC3" + secretKey).getBytes(StandardCharsets.UTF_8), date);
            byte[] secretService = hmacSha256(secretDate, service);
            byte[] secretSigning = hmacSha256(secretService, "tc3_request");
            String signature = bytesToHex(hmacSha256(secretSigning, stringToSign)).toLowerCase();

            return algorithm + " " +
                    "Credential=" + secretId + "/" + credentialScope + ", " +
                    "SignedHeaders=" + signedHeaders + ", " +
                    "Signature=" + signature;
        } catch (Exception e) {
            logger.error("生成腾讯云签名失败", e);
            throw new RuntimeException("签名生成失败: " + e.getMessage());
        }
    }

    private String getHostFromUrl(String url) {
        try {
            java.net.URL urlObj = new java.net.URL(url);
            return urlObj.getHost();
        } catch (Exception e) {
            return "hunyuan.tencentcloudapi.com";
        }
    }

    private byte[] hmacSha256(byte[] key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "HmacSHA256");
            mac.init(secretKeySpec);
            return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException("HMAC-SHA256计算失败", e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private String resolveProviderKey(String modelName) {
        if (isBlank(modelName)) {
            return llmConfig.getProvider();
        }
        String trimmed = modelName.trim();
        String mapped = MODEL_PROVIDER_MAP.get(trimmed);
        if (mapped != null) {
            return mapped;
        }
        mapped = MODEL_PROVIDER_MAP.get(trimmed.toLowerCase(Locale.ROOT));
        if (mapped != null) {
            return mapped;
        }
        return trimmed;
    }

    private boolean isHunyuan(String providerKey) {
        String normalized = providerKey == null ? null : providerKey.trim().toLowerCase(Locale.ROOT);
        return Objects.equals(normalized, "hunyuan") || Objects.equals(normalized, "tencent");
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (!isBlank(value)) {
                return value.trim();
            }
        }
        return null;
    }

    private <T> T firstNonNull(T... values) {
        if (values == null) {
            return null;
        }
        for (T value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }
}


