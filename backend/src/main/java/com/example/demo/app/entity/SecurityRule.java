// src/main/java/com/example/demo/app/entity/SecurityRule.java
package com.example.demo.app.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 安全规则表
 * 用于存储安全检测规则和话术规范
 */
@Data
public class SecurityRule {
    private Long id;
    private String ruleName;          // 规则名称
    private String ruleType;          // 规则类型：keyword-关键词, pattern-正则模式, ai-AI检测
    private String pattern;          // 匹配模式（正则表达式或关键词）
    private String category;          // 分类：crisis-危机, risk-风险, compliance-合规
    private Integer riskLevel;        // 风险级别：1-低，2-中，3-高，4-严重
    private String action;            // 处理动作：block-阻止, warn-警告, alert-告警, review-人工审核
    private String warningMessage;    // 警告消息
    private String guidance;          // 话术指导（建议的回复方式）
    private Integer status;           // 状态：0-禁用，1-启用
    private Integer priority;         // 优先级（数字越大优先级越高）
    private String description;       // 描述
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

