// src/main/java/com/example/demo/service/SystemConfigService.java
package com.example.demo.service;

import com.example.demo.dto.SystemConfigDTO;
import com.example.demo.dto.SystemConfigUpdateDTO;
import java.util.List;
import java.util.Map;

public interface SystemConfigService {

    // 配置管理
    SystemConfigDTO getConfigByKey(String configKey);
    List<SystemConfigDTO> getAllConfigs();
    List<SystemConfigDTO> getPublicConfigs();
    SystemConfigDTO updateConfig(SystemConfigUpdateDTO updateDTO);
    SystemConfigDTO updateConfigValue(String configKey, String configValue);
    void deleteConfig(String configKey);

    // 配置值获取（带类型转换）
    String getStringValue(String configKey, String defaultValue);
    Integer getIntValue(String configKey, Integer defaultValue);
    Boolean getBooleanValue(String configKey, Boolean defaultValue);

    // 批量操作
    Map<String, String> getAllConfigsAsMap();
    void updateMultipleConfigs(Map<String, String> configs);

    // 系统初始化
    void initializeSystemConfigs();
}