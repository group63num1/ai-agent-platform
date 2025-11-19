// src/main/java/com/example/demo/service/impl/SystemConfigServiceImpl.java
package com.example.demo.service.impl;

import com.example.demo.app.entity.SystemConfig;
import com.example.demo.app.mapper.SystemConfigMapper;
import com.example.demo.dto.SystemConfigDTO;
import com.example.demo.dto.SystemConfigUpdateDTO;
import com.example.demo.service.SystemConfigService;
import com.example.demo.util.BeanConvertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SystemConfigServiceImpl implements SystemConfigService {

    @Autowired
    private SystemConfigMapper systemConfigMapper;

    @Override
    public SystemConfigDTO getConfigByKey(String configKey) {
        SystemConfig config = systemConfigMapper.selectByKey(configKey);
        if (config == null) {
            throw new RuntimeException("配置项不存在: " + configKey);
        }
        return BeanConvertUtil.convert(config, SystemConfigDTO.class);
    }

    @Override
    public List<SystemConfigDTO> getAllConfigs() {
        List<SystemConfig> configs = systemConfigMapper.selectAll();
        return BeanConvertUtil.convertList(configs, SystemConfigDTO.class);
    }

    @Override
    public List<SystemConfigDTO> getPublicConfigs() {
        List<SystemConfig> configs = systemConfigMapper.selectByIsPublic(1);
        return BeanConvertUtil.convertList(configs, SystemConfigDTO.class);
    }

    @Override
    @Transactional
    public SystemConfigDTO updateConfig(SystemConfigUpdateDTO updateDTO) {
        SystemConfig existingConfig = systemConfigMapper.selectByKey(updateDTO.getConfigKey());

        if (existingConfig == null) {
            // 创建新配置
            SystemConfig newConfig = new SystemConfig();
            newConfig.setConfigKey(updateDTO.getConfigKey());
            newConfig.setConfigValue(updateDTO.getConfigValue());
            newConfig.setDescription(updateDTO.getDescription());
            newConfig.setType(updateDTO.getType() != null ? updateDTO.getType() : "string");
            newConfig.setIsPublic(updateDTO.getIsPublic() != null ? updateDTO.getIsPublic() : 0);

            int result = systemConfigMapper.insert(newConfig);
            if (result <= 0) {
                throw new RuntimeException("创建配置失败");
            }

            return getConfigByKey(updateDTO.getConfigKey());
        } else {
            // 更新现有配置
            existingConfig.setConfigValue(updateDTO.getConfigValue());
            if (updateDTO.getDescription() != null) {
                existingConfig.setDescription(updateDTO.getDescription());
            }
            if (updateDTO.getType() != null) {
                existingConfig.setType(updateDTO.getType());
            }
            if (updateDTO.getIsPublic() != null) {
                existingConfig.setIsPublic(updateDTO.getIsPublic());
            }

            int result = systemConfigMapper.update(existingConfig);
            if (result <= 0) {
                throw new RuntimeException("更新配置失败");
            }

            return getConfigByKey(updateDTO.getConfigKey());
        }
    }

    @Override
    @Transactional
    public SystemConfigDTO updateConfigValue(String configKey, String configValue) {
        SystemConfig existingConfig = systemConfigMapper.selectByKey(configKey);
        if (existingConfig == null) {
            throw new RuntimeException("配置项不存在: " + configKey);
        }

        int result = systemConfigMapper.updateValueByKey(configKey, configValue);
        if (result <= 0) {
            throw new RuntimeException("更新配置值失败");
        }

        return getConfigByKey(configKey);
    }

    @Override
    @Transactional
    public void deleteConfig(String configKey) {
        int result = systemConfigMapper.deleteByKey(configKey);
        if (result <= 0) {
            throw new RuntimeException("删除配置失败");
        }
    }

    @Override
    public String getStringValue(String configKey, String defaultValue) {
        try {
            SystemConfig config = systemConfigMapper.selectByKey(configKey);
            return config != null ? config.getConfigValue() : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    @Override
    public Integer getIntValue(String configKey, Integer defaultValue) {
        try {
            SystemConfig config = systemConfigMapper.selectByKey(configKey);
            return config != null ? Integer.parseInt(config.getConfigValue()) : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    @Override
    public Boolean getBooleanValue(String configKey, Boolean defaultValue) {
        try {
            SystemConfig config = systemConfigMapper.selectByKey(configKey);
            if (config == null) return defaultValue;

            String value = config.getConfigValue().toLowerCase();
            return "true".equals(value) || "1".equals(value) || "yes".equals(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    @Override
    public Map<String, String> getAllConfigsAsMap() {
        List<SystemConfig> configs = systemConfigMapper.selectAll();
        Map<String, String> configMap = new HashMap<>();

        for (SystemConfig config : configs) {
            configMap.put(config.getConfigKey(), config.getConfigValue());
        }

        return configMap;
    }

    @Override
    @Transactional
    public void updateMultipleConfigs(Map<String, String> configs) {
        for (Map.Entry<String, String> entry : configs.entrySet()) {
            SystemConfig existingConfig = systemConfigMapper.selectByKey(entry.getKey());
            if (existingConfig != null) {
                systemConfigMapper.updateValueByKey(entry.getKey(), entry.getValue());
            }
        }
    }

    @Override
    @Transactional
    public void initializeSystemConfigs() {
        // 初始化系统默认配置
        initializeConfig("site_name", "用户管理系统", "网站名称", "string", 1);
        initializeConfig("site_description", "基于RBAC的用户管理系统", "网站描述", "string", 1);
        initializeConfig("user_register_enabled", "true", "是否允许用户注册", "boolean", 1);
        initializeConfig("login_max_attempts", "5", "登录最大失败次数", "number", 0);
        initializeConfig("login_lock_duration", "30", "登录锁定时长（分钟）", "number", 0);
        initializeConfig("max_teams_per_user", "10", "每个用户最大创建团队数", "number", 0);
    }

    private void initializeConfig(String key, String value, String description, String type, Integer isPublic) {
        SystemConfig existing = systemConfigMapper.selectByKey(key);
        if (existing == null) {
            SystemConfig config = new SystemConfig();
            config.setConfigKey(key);
            config.setConfigValue(value);
            config.setDescription(description);
            config.setType(type);
            config.setIsPublic(isPublic);
            systemConfigMapper.insert(config);
        }
    }
}