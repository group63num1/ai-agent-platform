// src/main/java/com/example/demo/controller/PermissionController.java
package com.example.demo.controller;

import com.example.demo.common.ApiResponse;
import com.example.demo.dto.PermissionDTO;
import com.example.demo.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/permissions")
public class PermissionController {

    @Autowired
    private PermissionService permissionService;

    /**
     * 获取所有权限列表mvn
     */
    @GetMapping
    public ApiResponse<List<PermissionDTO>> getAllPermissions() {
        try {
            List<PermissionDTO> permissions = permissionService.getAllPermissions();
            return ApiResponse.ok(permissions);
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    /**
     * 根据模块获取权限
     */
    @GetMapping("/module/{module}")
    public ApiResponse<List<PermissionDTO>> getPermissionsByModule(@PathVariable String module) {
        try {
            List<PermissionDTO> permissions = permissionService.getPermissionsByModule(module);
            return ApiResponse.ok(permissions);
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    /**
     * 获取所有模块列表
     */
    @GetMapping("/modules")
    public ApiResponse<List<String>> getAllModules() {
        try {
            List<String> modules = permissionService.getAllModules();
            return ApiResponse.ok(modules);
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    /**
     * 获取用户的权限
     */
    @GetMapping("/user/{userId}")
    public ApiResponse<List<PermissionDTO>> getPermissionsByUserId(@PathVariable Long userId) {
        try {
            List<PermissionDTO> permissions = permissionService.getPermissionsByUserId(userId);
            return ApiResponse.ok(permissions);
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    /**
     * 获取角色的权限
     */
    @GetMapping("/role/{roleId}")
    public ApiResponse<List<PermissionDTO>> getPermissionsByRoleId(@PathVariable Long roleId) {
        try {
            List<PermissionDTO> permissions = permissionService.getPermissionsByRoleId(roleId);
            return ApiResponse.ok(permissions);
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }
}
