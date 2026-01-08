// src/main/java/com/example/demo/service/RoleService.java
package com.example.demo.service;

import com.example.demo.dto.RoleDTO;
import java.util.List;

public interface RoleService {

    // 创建角色
    RoleDTO createRole(RoleDTO roleDTO);

    // 获取所有角色
    List<RoleDTO> getAllRoles();

    // 根据ID获取角色
    RoleDTO getRoleById(Long id);

    // 更新角色
    RoleDTO updateRole(Long id, RoleDTO roleDTO);

    // 删除角色
    void deleteRole(Long id);

    // 为用户分配角色
    void assignRolesToUser(Long userId, List<Long> roleIds);

    // 获取用户的角色
    List<RoleDTO> getRolesByUserId(Long userId);
}