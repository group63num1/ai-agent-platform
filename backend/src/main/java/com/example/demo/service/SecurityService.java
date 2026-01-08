// src/main/java/com/example/demo/service/SecurityService.java
package com.example.demo.service;

import com.example.demo.dto.SecurityCheckResultDTO;

/**
 * 安全服务接口
 * 提供敏感内容过滤、风险检测、隐私保护等功能
 */
public interface SecurityService {

    /**
     * 检查消息内容的安全性
     * @param content 消息内容
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @param ipAddress IP地址
     * @param userAgent 用户代理
     * @return 安全检查结果
     */
    SecurityCheckResultDTO checkContent(String content, Long userId, Long sessionId, 
                                        String ipAddress, String userAgent);

    /**
     * 过滤敏感词
     * @param content 原始内容
     * @return 过滤后的内容
     */
    String filterSensitiveWords(String content);

    /**
     * 检测隐私数据
     * @param content 消息内容
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @param source 数据来源：user_input, ai_response, system
     */
    void detectPrivacyData(String content, Long userId, Long sessionId, String source);

    /**
     * 记录风险事件
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @param messageId 消息ID
     * @param riskType 风险类型
     * @param riskLevel 风险级别
     * @param content 触发风险的内容
     * @param matchedRule 匹配的规则
     * @param action 处理动作
     * @param ipAddress IP地址
     * @param userAgent 用户代理
     */
    void recordRisk(Long userId, Long sessionId, Long messageId, String riskType, 
                    Integer riskLevel, String content, String matchedRule, String action,
                    String ipAddress, String userAgent);
}

