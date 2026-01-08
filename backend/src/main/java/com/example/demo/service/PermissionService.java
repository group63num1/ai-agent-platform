// src/main/java/com/example/demo/service/PermissionService.java
package com.example.demo.service;

import com.example.demo.dto.PermissionDTO;
import java.util.List;

public interface PermissionService {

    // 获取所有权限
    List<PermissionDTO> getAllPermissions();

    // 根据模块获取权限
    List<PermissionDTO> getPermissionsByModule(String module);

    // 获取所有模块
    List<String> getAllModules();

    // 获取用户的权限
    List<PermissionDTO> getPermissionsByUserId(Long userId);

    // 获取角色的权限
    List<PermissionDTO> getPermissionsByRoleId(Long roleId);
}