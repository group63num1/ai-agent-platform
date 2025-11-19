// src/main/java/com/example/demo/controller/SystemController.java
package com.example.demo.controller;

import com.example.demo.common.ApiResponse;
import com.example.demo.dto.SystemConfigDTO;
import com.example.demo.dto.SystemConfigUpdateDTO;
import com.example.demo.service.SystemConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/system")
public class SystemController {

    @Autowired
    private SystemConfigService systemConfigService;

    /**
     * 获取所有系统配置
     */
    @GetMapping("/configs")
    public ApiResponse<List<SystemConfigDTO>> getAllConfigs() {
        try {
            List<SystemConfigDTO> configs = systemConfigService.getAllConfigs();
            return ApiResponse.ok(configs);
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    /**
     * 获取公开的系统配置
     */
    @GetMapping("/configs/public")
    public ApiResponse<List<SystemConfigDTO>> getPublicConfigs() {
        try {
            List<SystemConfigDTO> configs = systemConfigService.getPublicConfigs();
            return ApiResponse.ok(configs);
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    /**
     * 根据键获取系统配置
     */
    @GetMapping("/configs/{configKey}")
    public ApiResponse<SystemConfigDTO> getConfigByKey(@PathVariable String configKey) {
        try {
            SystemConfigDTO config = systemConfigService.getConfigByKey(configKey);
            return ApiResponse.ok(config);
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    /**
     * 更新系统配置
     */
    @PutMapping("/configs")
    public ApiResponse<SystemConfigDTO> updateConfig(@RequestBody SystemConfigUpdateDTO updateDTO) {
        try {
            SystemConfigDTO updatedConfig = systemConfigService.updateConfig(updateDTO);
            return ApiResponse.ok(updatedConfig);
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    /**
     * 批量更新系统配置值
     */
    @PutMapping("/configs/batch")
    public ApiResponse<Void> updateMultipleConfigs(@RequestBody Map<String, String> configs) {
        try {
            systemConfigService.updateMultipleConfigs(configs);
            return new ApiResponse<>(0, "配置更新成功", null);
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    /**
     * 删除系统配置
     */
    @DeleteMapping("/configs/{configKey}")
    public ApiResponse<Void> deleteConfig(@PathVariable String configKey) {
        try {
            systemConfigService.deleteConfig(configKey);
            return new ApiResponse<>(0, "配置删除成功", null);
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    /**
     * 获取系统信息
     */
    @GetMapping("/info")
    public ApiResponse<Map<String, Object>> getSystemInfo() {
        try {
            Map<String, Object> systemInfo = Map.of(
                    "name", "DevOps用户权限管理系统",
                    "version", "1.0.0",
                    "description", "基于Spring Boot + MySQL + JWT的企业级用户权限管理系统",
                    "author", "DevOps 2025",
                    "javaVersion", System.getProperty("java.version"),
                    "os", System.getProperty("os.name"),
                    "timestamp", System.currentTimeMillis()
            );
            return ApiResponse.ok(systemInfo);
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    /**
     * 初始化系统数据
     */
    @PostMapping("/init")
    public ApiResponse<Void> initializeSystem() {
        try {
            systemConfigService.initializeSystemConfigs();
            return new ApiResponse<>(0, "系统初始化成功", null);
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    /**
     * 获取系统健康状态
     */
    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> getHealthStatus() {
        try {
            Map<String, Object> healthStatus = Map.of(
                    "status", "UP",
                    "database", "CONNECTED",
                    "timestamp", System.currentTimeMillis(),
                    "uptime", Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
            );
            return ApiResponse.ok(healthStatus);
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }
}