// src/main/java/com/example/demo/dto/SecurityCheckResultDTO.java
package com.example.demo.dto;

import lombok.Data;
import java.util.List;

/**
 * 安全检查结果DTO
 */
@Data
public class SecurityCheckResultDTO {
    private Boolean passed;              // 是否通过检查
    private Boolean blocked;             // 是否被阻止
    private Integer riskLevel;           // 风险级别：1-低，2-中，3-高，4-严重
    private String filteredContent;      // 过滤后的内容
    private String warningMessage;       // 警告消息
    private String guidance;            // 话术指导
    private List<String> matchedWords;  // 匹配的敏感词
    private List<String> matchedRules;   // 匹配的规则
    private String action;               // 处理动作：block-阻止, warn-警告, alert-告警, review-人工审核
}

