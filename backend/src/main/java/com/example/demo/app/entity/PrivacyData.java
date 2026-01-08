// src/main/java/com/example/demo/app/entity/PrivacyData.java
package com.example.demo.app.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 隐私数据表
 * 用于记录和追踪敏感隐私数据的使用情况
 */
@Data
public class PrivacyData {
    private Long id;
    private Long userId;              // 用户ID
    private Long sessionId;           // 会话ID
    private String dataType;          // 数据类型：phone-手机号, email-邮箱, idcard-身份证, bankcard-银行卡, address-地址等
    private String dataValue;          // 数据值（加密存储）
    private String dataHash;          // 数据哈希值（用于去重和检索）
    private String source;            // 数据来源：user_input-用户输入, ai_response-AI回复, system-系统
    private String action;            // 操作类型：detected-检测到, stored-已存储, deleted-已删除, masked-已脱敏
    private Integer retentionDays;     // 保留天数（数据最小化策略）
    private LocalDateTime expiresAt;  // 过期时间（自动删除时间）
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;  // 删除时间（软删除）
}

