// src/main/java/com/example/demo/app/mapper/SecurityRuleMapper.java
package com.example.demo.app.mapper;

import com.example.demo.app.entity.SecurityRule;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SecurityRuleMapper {
    int insert(SecurityRule securityRule);
    SecurityRule selectById(Long id);
    List<SecurityRule> selectAll();
    List<SecurityRule> selectByType(String ruleType);
    List<SecurityRule> selectByCategory(String category);
    List<SecurityRule> selectByRiskLevel(Integer riskLevel);
    List<SecurityRule> selectActive(); // 查询启用的规则，按优先级排序
    int update(SecurityRule securityRule);
    int delete(Long id);
}

