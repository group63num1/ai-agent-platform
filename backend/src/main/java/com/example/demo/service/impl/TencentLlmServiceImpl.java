// src/main/java/com/example/demo/service/impl/TencentLlmServiceImpl.java
package com.example.demo.service.impl;

import com.example.demo.config.LlmConfig;
import com.example.demo.service.LlmService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.*;

public class TencentLlmServiceImpl implements LlmService {

    private static final Logger logger = LoggerFactory.getLogger(TencentLlmServiceImpl.class);

    @Autowired
    private LlmConfig llmConfig;

    @Autowired
    private RestTemplate restTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String generateResponse(List<Map<String, String>> messages) {
        // 如果没有配置API密钥，返回默认回复
        String apiKey = llmConfig.getApiKey();
        if (apiKey == null || apiKey.trim().isEmpty()) {
            logger.warn("LLM API密钥未配置，返回默认回复");
            return "抱歉，AI服务暂未配置。请配置LLM API密钥后使用。";
        }

        try {
            // 腾讯云混元大模型API配置
            // API密钥格式: SecretId:SecretKey (用冒号分隔)
            String[] credentials = apiKey.split(":", 2);
            if (credentials.length != 2) {
                throw new RuntimeException("腾讯云API密钥格式错误，应为 SecretId:SecretKey");
            }
            String secretId = credentials[0].trim();
            String secretKey = credentials[1].trim();

            // 构建请求体（腾讯云混元API格式）
            // 注意：腾讯云API要求参数名首字母大写
            Map<String, Object> requestBody = new HashMap<>();
            String model = llmConfig.getModel();
            if (model == null || model.trim().isEmpty()) {
                model = "hunyuan-lite"; // 默认模型
            }
            requestBody.put("Model", model); // 注意：必须是 "Model"（大写M）
            
            // 转换消息格式为腾讯云格式
            List<Map<String, Object>> tencentMessages = new ArrayList<>();
            for (Map<String, String> msg : messages) {
                Map<String, Object> tencentMsg = new HashMap<>();
                String role = msg.get("role");
                // 腾讯云使用 Role 字段，user -> user, assistant -> assistant, system -> system
                tencentMsg.put("Role", role);
                tencentMsg.put("Content", msg.get("content"));
                tencentMessages.add(tencentMsg);
            }
            requestBody.put("Messages", tencentMessages);
            // 腾讯云混元API只支持必需的参数：Model 和 Messages
            // 其他参数（如MaxTokens、Temperature）可能在不同版本中支持情况不同
            // 如果API不支持这些参数，则只使用必需参数
            
            // 记录请求体（用于调试）
            logger.debug("腾讯云API请求体: {}", requestBody);

            // 构建请求URL和参数
            String apiUrl = llmConfig.getApiUrl();
            if (apiUrl == null || apiUrl.trim().isEmpty()) {
                // 默认使用腾讯云混元大模型API
                apiUrl = "https://hunyuan.tencentcloudapi.com";
            }

            // 腾讯云API需要签名认证
            String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
            String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            
            // 构建请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-TC-Action", "ChatCompletions");
            headers.set("X-TC-Version", "2023-09-01");
            headers.set("X-TC-Timestamp", timestamp);
            headers.set("X-TC-Region", "ap-beijing"); // 根据需要修改区域
            
            // 生成签名
            String authorization = generateTencentSignature(
                    secretId, secretKey, "POST", apiUrl, 
                    requestBody, timestamp, date);
            headers.set("Authorization", authorization);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            // 发送请求
            logger.info("调用腾讯云混元大模型API: {}", apiUrl);
            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            // 解析响应（腾讯云API响应格式）
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode jsonNode;
                try {
                    jsonNode = objectMapper.readTree(response.getBody());
                } catch (JsonProcessingException e) {
                    logger.error("解析腾讯云API响应失败", e);
                    throw new RuntimeException("解析AI服务响应失败: " + e.getMessage());
                }
                
                // 检查是否有错误
                if (jsonNode.has("Response") && jsonNode.get("Response").has("Error")) {
                    JsonNode error = jsonNode.get("Response").get("Error");
                    String errorMsg = error.has("Message") ? error.get("Message").asText() : "未知错误";
                    throw new RuntimeException("腾讯云API调用失败: " + errorMsg);
                }
                
                // 解析成功响应
                JsonNode responseNode = jsonNode.get("Response");
                if (responseNode != null && responseNode.has("Choices") && responseNode.get("Choices").isArray()) {
                    JsonNode choices = responseNode.get("Choices");
                    if (choices.size() > 0) {
                        JsonNode choice = choices.get(0);
                        if (choice.has("Message") && choice.get("Message").has("Content")) {
                            String content = choice.get("Message").get("Content").asText();
                            logger.info("腾讯云API调用成功，返回内容长度: {}", content.length());
                            return content;
                        }
                    }
                }
                throw new RuntimeException("腾讯云API返回格式异常");
            } else {
                throw new RuntimeException("腾讯云API调用失败: " + response.getStatusCode());
            }

        } catch (org.springframework.web.client.ResourceAccessException e) {
            logger.error("腾讯云API调用超时或网络错误", e);
            throw new RuntimeException("AI服务连接超时，请稍后重试");
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            logger.error("腾讯云API调用失败: {}", e.getResponseBodyAsString(), e);
            throw new RuntimeException("AI服务调用失败: " + e.getMessage() + " - " + e.getResponseBodyAsString());
        } catch (RuntimeException e) {
            // 如果是RuntimeException（包括我们自定义的异常），直接抛出
            throw e;
        } catch (Exception e) {
            logger.error("腾讯云API调用异常", e);
            // 将所有已检查异常包装成RuntimeException
            throw new RuntimeException("AI服务调用异常: " + e.getMessage(), e);
        }
    }

    /**
     * 生成腾讯云API签名
     */
    private String generateTencentSignature(String secretId, String secretKey, 
                                           String method, String url,
                                           Map<String, Object> requestBody,
                                           String timestamp, String date) {
        try {
            // 简化版签名（适用于部分腾讯云API）
            // 注意：这里使用简化实现，实际应该使用完整的TC3-HMAC-SHA256签名算法
            
            // 构建待签名字符串
            String service = "hunyuan";
            String httpRequestMethod = method;
            String canonicalUri = "/";
            String canonicalQueryString = "";
            String canonicalHeaders = "content-type:application/json\nhost:" + getHostFromUrl(url) + "\n";
            String signedHeaders = "content-type;host";
            
            // 请求体哈希
            String payload;
            try {
                payload = objectMapper.writeValueAsString(requestBody);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("序列化请求体失败", e);
            }
            MessageDigest md;
            try {
                md = MessageDigest.getInstance("SHA-256");
            } catch (java.security.NoSuchAlgorithmException e) {
                throw new RuntimeException("SHA-256算法不可用", e);
            }
            byte[] hashBytes = md.digest(payload.getBytes(StandardCharsets.UTF_8));
            String hashedRequestPayload = bytesToHex(hashBytes).toLowerCase();
            
            // 构建规范请求
            String canonicalRequest = httpRequestMethod + "\n" +
                    canonicalUri + "\n" +
                    canonicalQueryString + "\n" +
                    canonicalHeaders + "\n" +
                    signedHeaders + "\n" +
                    hashedRequestPayload;
            
            // 构建待签名字符串
            String algorithm = "TC3-HMAC-SHA256";
            String credentialScope = date + "/" + service + "/tc3_request";
            String hashedCanonicalRequest = bytesToHex(md.digest(canonicalRequest.getBytes(StandardCharsets.UTF_8))).toLowerCase();
            String stringToSign = algorithm + "\n" +
                    timestamp + "\n" +
                    credentialScope + "\n" +
                    hashedCanonicalRequest;
            
            // 计算签名
            byte[] secretDate = hmacSha256(("TC3" + secretKey).getBytes(StandardCharsets.UTF_8), date);
            byte[] secretService = hmacSha256(secretDate, service);
            byte[] secretSigning = hmacSha256(secretService, "tc3_request");
            String signature = bytesToHex(hmacSha256(secretSigning, stringToSign)).toLowerCase();
            
            // 构建Authorization
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
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}

