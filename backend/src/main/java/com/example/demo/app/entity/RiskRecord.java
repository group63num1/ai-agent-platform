// src/main/java/com/example/demo/app/entity/RiskRecord.java
package com.example.demo.app.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 风险记录表
 * 用于记录检测到的风险事件
 */
@Data
public class RiskRecord {
    private Long id;
    private Long userId;              // 用户ID
    private Long sessionId;           // 会话ID
    private Long messageId;            // 消息ID
    private String riskType;          // 风险类型：sensitive-敏感词, crisis-危机, privacy-隐私泄露
    private Integer riskLevel;        // 风险级别：1-低，2-中，3-高，4-严重
    private String content;           // 触发风险的内容
    private String matchedRule;       // 匹配的规则（规则名称或敏感词）
    private String action;            // 处理动作：blocked-已阻止, warned-已警告, alerted-已告警, reviewed-已审核
    private String details;           // 详细信息（JSON格式）
    private String ipAddress;         // IP地址
    private String userAgent;         // 用户代理
    private LocalDateTime createdAt;
}

