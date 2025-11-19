// src/main/java/com/example/demo/service/impl/SecurityServiceImpl.java
package com.example.demo.service.impl;

import com.example.demo.app.entity.*;
import com.example.demo.app.mapper.*;
import com.example.demo.dto.SecurityCheckResultDTO;
import com.example.demo.service.SecurityService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class SecurityServiceImpl implements SecurityService {

    @Autowired
    private SensitiveWordMapper sensitiveWordMapper;

    @Autowired
    private SecurityRuleMapper securityRuleMapper;

    @Autowired
    private RiskRecordMapper riskRecordMapper;

    @Autowired
    private PrivacyDataMapper privacyDataMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 缓存敏感词列表（提高性能）
    private List<SensitiveWord> cachedSensitiveWords = null;
    private long lastCacheTime = 0;
    private static final long CACHE_DURATION = 5 * 60 * 1000; // 5分钟缓存

    // 缓存安全规则列表
    private List<SecurityRule> cachedSecurityRules = null;
    private long lastRulesCacheTime = 0;

    @Override
    @Transactional
    public SecurityCheckResultDTO checkContent(String content, Long userId, Long sessionId,
                                               String ipAddress, String userAgent) {
        SecurityCheckResultDTO result = new SecurityCheckResultDTO();
        result.setPassed(true);
        result.setBlocked(false);
        result.setRiskLevel(1);
        result.setFilteredContent(content);
        result.setMatchedWords(new ArrayList<>());
        result.setMatchedRules(new ArrayList<>());
        result.setAction("pass");

        if (content == null || content.trim().isEmpty()) {
            return result;
        }

        // 1. 敏感词检测
        List<SensitiveWord> matchedSensitiveWords = detectSensitiveWords(content);
        if (!matchedSensitiveWords.isEmpty()) {
            result.getMatchedWords().addAll(
                matchedSensitiveWords.stream()
                    .map(SensitiveWord::getWord)
                    .collect(Collectors.toList())
            );

            // 找到最高风险级别
            int maxLevel = matchedSensitiveWords.stream()
                .mapToInt(SensitiveWord::getLevel)
                .max()
                .orElse(1);

            result.setRiskLevel(Math.max(result.getRiskLevel(), maxLevel));

            // 根据敏感词的处理动作决定如何处理
            for (SensitiveWord word : matchedSensitiveWords) {
                if ("block".equals(word.getAction())) {
                    result.setBlocked(true);
                    result.setPassed(false);
                    result.setAction("block");
                    result.setWarningMessage("消息包含敏感内容，已被阻止");
                    break;
                } else if ("warn".equals(word.getAction())) {
                    result.setAction("warn");
                    result.setWarningMessage("消息包含敏感内容，请注意");
                } else if ("replace".equals(word.getAction()) && word.getReplacement() != null) {
                    // 替换敏感词
                    result.setFilteredContent(
                        result.getFilteredContent().replace(word.getWord(), word.getReplacement())
                    );
                }
            }
        }

        // 2. 安全规则检测
        List<SecurityRule> matchedRules = detectSecurityRules(content);
        if (!matchedRules.isEmpty()) {
            result.getMatchedRules().addAll(
                matchedRules.stream()
                    .map(SecurityRule::getRuleName)
                    .collect(Collectors.toList())
            );

            // 找到最高风险级别和优先级
            SecurityRule highestPriorityRule = matchedRules.stream()
                .max(Comparator.comparing(SecurityRule::getPriority)
                    .thenComparing(SecurityRule::getRiskLevel))
                .orElse(null);

            if (highestPriorityRule != null) {
                result.setRiskLevel(Math.max(result.getRiskLevel(), highestPriorityRule.getRiskLevel()));

                // 根据规则的处理动作决定如何处理
                String action = highestPriorityRule.getAction();
                result.setAction(action);

                if ("block".equals(action)) {
                    result.setBlocked(true);
                    result.setPassed(false);
                    result.setWarningMessage(highestPriorityRule.getWarningMessage());
                } else if ("warn".equals(action)) {
                    result.setWarningMessage(highestPriorityRule.getWarningMessage());
                    result.setGuidance(highestPriorityRule.getGuidance());
                } else if ("alert".equals(action)) {
                    result.setWarningMessage(highestPriorityRule.getWarningMessage());
                    result.setGuidance(highestPriorityRule.getGuidance());
                    // 记录高风险事件
                    recordRisk(userId, sessionId, null, "crisis", highestPriorityRule.getRiskLevel(),
                        content, highestPriorityRule.getRuleName(), "alerted", ipAddress, userAgent);
                } else if ("review".equals(action)) {
                    result.setWarningMessage("消息需要人工审核");
                    result.setGuidance(highestPriorityRule.getGuidance());
                }
            }
        }

        // 3. 如果被阻止，不继续处理
        if (result.getBlocked()) {
            return result;
        }

        // 4. 隐私数据检测
        detectPrivacyData(content, userId, sessionId, "user_input");

        // 5. 如果检测到风险，记录风险事件
        if (!result.getMatchedWords().isEmpty() || !result.getMatchedRules().isEmpty()) {
            String riskType = !result.getMatchedWords().isEmpty() ? "sensitive" : "crisis";
            recordRisk(userId, sessionId, null, riskType, result.getRiskLevel(),
                content, String.join(",", result.getMatchedRules()), result.getAction(),
                ipAddress, userAgent);
        }

        return result;
    }

    @Override
    public String filterSensitiveWords(String content) {
        if (content == null || content.trim().isEmpty()) {
            return content;
        }

        List<SensitiveWord> sensitiveWords = getCachedSensitiveWords();
        String filteredContent = content;

        for (SensitiveWord word : sensitiveWords) {
            if (word.getStatus() == 1 && "replace".equals(word.getAction())) {
                String replacement = word.getReplacement() != null ? word.getReplacement() : "***";
                filteredContent = filteredContent.replace(word.getWord(), replacement);
            }
        }

        return filteredContent;
    }

    @Override
    @Transactional
    public void detectPrivacyData(String content, Long userId, Long sessionId, String source) {
        if (content == null || content.trim().isEmpty()) {
            return;
        }

        // 检测手机号
        Pattern phonePattern = Pattern.compile("1[3-9]\\d{9}");
        java.util.regex.Matcher phoneMatcher = phonePattern.matcher(content);
        while (phoneMatcher.find()) {
            String phone = phoneMatcher.group();
            savePrivacyData(userId, sessionId, "phone", phone, source);
        }

        // 检测邮箱
        Pattern emailPattern = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
        java.util.regex.Matcher emailMatcher = emailPattern.matcher(content);
        while (emailMatcher.find()) {
            String email = emailMatcher.group();
            savePrivacyData(userId, sessionId, "email", email, source);
        }

        // 检测身份证号
        Pattern idCardPattern = Pattern.compile("\\d{17}[\\dXx]");
        java.util.regex.Matcher idCardMatcher = idCardPattern.matcher(content);
        while (idCardMatcher.find()) {
            String idCard = idCardMatcher.group();
            savePrivacyData(userId, sessionId, "idcard", idCard, source);
        }

        // 检测银行卡号
        Pattern bankCardPattern = Pattern.compile("\\d{16,19}");
        java.util.regex.Matcher bankCardMatcher = bankCardPattern.matcher(content);
        while (bankCardMatcher.find()) {
            String bankCard = bankCardMatcher.group();
            // 简单判断：16-19位数字可能是银行卡号
            if (bankCard.length() >= 16 && bankCard.length() <= 19) {
                savePrivacyData(userId, sessionId, "bankcard", bankCard, source);
            }
        }
    }

    @Override
    @Transactional
    public void recordRisk(Long userId, Long sessionId, Long messageId, String riskType,
                          Integer riskLevel, String content, String matchedRule, String action,
                          String ipAddress, String userAgent) {
        RiskRecord riskRecord = new RiskRecord();
        riskRecord.setUserId(userId);
        riskRecord.setSessionId(sessionId);
        riskRecord.setMessageId(messageId);
        riskRecord.setRiskType(riskType);
        riskRecord.setRiskLevel(riskLevel);
        riskRecord.setContent(content);
        riskRecord.setMatchedRule(matchedRule);
        riskRecord.setAction(action);
        riskRecord.setIpAddress(ipAddress);
        riskRecord.setUserAgent(userAgent);

        try {
            Map<String, Object> details = new HashMap<>();
            details.put("timestamp", LocalDateTime.now().toString());
            details.put("riskType", riskType);
            details.put("riskLevel", riskLevel);
            riskRecord.setDetails(objectMapper.writeValueAsString(details));
        } catch (Exception e) {
            riskRecord.setDetails("{}");
        }

        riskRecordMapper.insert(riskRecord);
    }

    /**
     * 检测敏感词
     */
    private List<SensitiveWord> detectSensitiveWords(String content) {
        List<SensitiveWord> matchedWords = new ArrayList<>();
        List<SensitiveWord> sensitiveWords = getCachedSensitiveWords();

        for (SensitiveWord word : sensitiveWords) {
            if (word.getStatus() == 1 && content.contains(word.getWord())) {
                matchedWords.add(word);
            }
        }

        return matchedWords;
    }

    /**
     * 检测安全规则
     */
    private List<SecurityRule> detectSecurityRules(String content) {
        List<SecurityRule> matchedRules = new ArrayList<>();
        List<SecurityRule> securityRules = getCachedSecurityRules();

        for (SecurityRule rule : securityRules) {
            if (rule.getStatus() == 1) {
                boolean matched = false;

                if ("keyword".equals(rule.getRuleType())) {
                    // 关键词匹配
                    if (rule.getPattern() != null && content.contains(rule.getPattern())) {
                        matched = true;
                    }
                } else if ("pattern".equals(rule.getRuleType())) {
                    // 正则表达式匹配
                    try {
                        Pattern pattern = Pattern.compile(rule.getPattern());
                        java.util.regex.Matcher matcher = pattern.matcher(content);
                        if (matcher.find()) {
                            matched = true;
                        }
                    } catch (Exception e) {
                        // 正则表达式错误，跳过
                    }
                }

                if (matched) {
                    matchedRules.add(rule);
                }
            }
        }

        return matchedRules;
    }

    /**
     * 保存隐私数据
     */
    private void savePrivacyData(Long userId, Long sessionId, String dataType, 
                                 String dataValue, String source) {
        try {
            // 计算数据哈希值
            String dataHash = calculateHash(dataValue);

            // 检查是否已存在（去重）
            List<PrivacyData> existing = privacyDataMapper.selectByDataHash(dataHash);
            if (!existing.isEmpty()) {
                return; // 已存在，不重复保存
            }

            PrivacyData privacyData = new PrivacyData();
            privacyData.setUserId(userId);
            privacyData.setSessionId(sessionId);
            privacyData.setDataType(dataType);
            privacyData.setDataValue(encryptData(dataValue)); // 加密存储
            privacyData.setDataHash(dataHash);
            privacyData.setSource(source);
            privacyData.setAction("detected");

            // 设置保留天数（数据最小化策略）
            int retentionDays = getRetentionDays(dataType);
            privacyData.setRetentionDays(retentionDays);
            privacyData.setExpiresAt(LocalDateTime.now().plusDays(retentionDays));

            privacyDataMapper.insert(privacyData);
        } catch (Exception e) {
            // 记录错误但不影响主流程
            e.printStackTrace();
        }
    }

    /**
     * 获取缓存的敏感词列表
     */
    private List<SensitiveWord> getCachedSensitiveWords() {
        long currentTime = System.currentTimeMillis();
        if (cachedSensitiveWords == null || 
            (currentTime - lastCacheTime) > CACHE_DURATION) {
            cachedSensitiveWords = sensitiveWordMapper.selectActive();
            lastCacheTime = currentTime;
        }
        return cachedSensitiveWords;
    }

    /**
     * 获取缓存的安全规则列表
     */
    private List<SecurityRule> getCachedSecurityRules() {
        long currentTime = System.currentTimeMillis();
        if (cachedSecurityRules == null || 
            (currentTime - lastRulesCacheTime) > CACHE_DURATION) {
            cachedSecurityRules = securityRuleMapper.selectActive();
            lastRulesCacheTime = currentTime;
        }
        return cachedSecurityRules;
    }

    /**
     * 计算数据哈希值
     */
    private String calculateHash(String data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return data; // 如果哈希失败，返回原值
        }
    }

    /**
     * 加密数据（简单实现，实际应该使用更安全的加密算法）
     */
    private String encryptData(String data) {
        // TODO: 实现真正的加密算法（如AES）
        // 这里先简单返回，实际应该加密
        return data;
    }

    /**
     * 获取数据保留天数（数据最小化策略）
     */
    private int getRetentionDays(String dataType) {
        // 根据数据类型设置不同的保留天数
        switch (dataType) {
            case "phone":
            case "email":
                return 30; // 联系方式保留30天
            case "idcard":
            case "bankcard":
                return 7;  // 敏感证件保留7天
            default:
                return 90; // 默认保留90天
        }
    }
}

