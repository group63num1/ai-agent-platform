// src/main/java/com/example/demo/app/entity/SensitiveWord.java
package com.example.demo.app.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 敏感词表
 * 用于存储需要过滤的敏感词汇
 */
@Data
public class SensitiveWord {
    private Long id;
    private String word;              // 敏感词
    private String category;          // 分类：violence-暴力, porn-色情, politics-政治, fraud-诈骗, privacy-隐私等
    private Integer level;            // 风险级别：1-低风险，2-中风险，3-高风险
    private String action;            // 处理动作：block-阻止, warn-警告, replace-替换
    private String replacement;      // 替换词（当action为replace时使用）
    private Integer status;           // 状态：0-禁用，1-启用
    private String description;       // 描述
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

