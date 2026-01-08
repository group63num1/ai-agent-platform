// src/main/java/com/example/demo/service/impl/RoleServiceImpl.java
package com.example.demo.service.impl;

import com.example.demo.dto.RoleDTO;
import com.example.demo.app.entity.Role;
import com.example.demo.app.mapper.RoleMapper;
import com.example.demo.service.RoleService;
import com.example.demo.util.BeanConvertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleMapper roleMapper;

    @Override
    public RoleDTO createRole(RoleDTO roleDTO) {
        // 检查角色代码是否已存在
        Role existingRole = roleMapper.selectByCode(roleDTO.getCode());
        if (existingRole != null) {
            throw new RuntimeException("角色代码已存在");
        }

        Role role = BeanConvertUtil.convert(roleDTO, Role.class);
        role.setIsSystem(0); // 默认非系统角色
        role.setStatus(1);   // 默认启用

        int result = roleMapper.insert(role);
        if (result <= 0) {
            throw new RuntimeException("创建角色失败");
        }

        return BeanConvertUtil.convert(role, RoleDTO.class);
    }

    @Override
    public List<RoleDTO> getAllRoles() {
        List<Role> roles = roleMapper.selectAll();
        return BeanConvertUtil.convertList(roles, RoleDTO.class);
    }

    @Override
    public RoleDTO getRoleById(Long id) {
        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new RuntimeException("角色不存在");
        }
        return BeanConvertUtil.convert(role, RoleDTO.class);
    }

    @Override
    public RoleDTO updateRole(Long id, RoleDTO roleDTO) {
        Role existingRole = roleMapper.selectById(id);
        if (existingRole == null) {
            throw new RuntimeException("角色不存在");
        }

        // 系统角色不能修改某些属性
        if (existingRole.getIsSystem() == 1) {
            throw new RuntimeException("系统角色不允许修改");
        }

        existingRole.setName(roleDTO.getName());
        existingRole.setDescription(roleDTO.getDescription());
        existingRole.setStatus(roleDTO.getStatus());
        existingRole.setSortOrder(roleDTO.getSortOrder());

        int result = roleMapper.update(existingRole);
        if (result <= 0) {
            throw new RuntimeException("更新角色失败");
        }

        return BeanConvertUtil.convert(existingRole, RoleDTO.class);
    }

    @Override
    public void deleteRole(Long id) {
        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new RuntimeException("角色不存在");
        }

        if (role.getIsSystem() == 1) {
            throw new RuntimeException("系统角色不允许删除");
        }

        int result = roleMapper.softDelete(id);
        if (result <= 0) {
            throw new RuntimeException("删除角色失败");
        }
    }

    @Override
    public void assignRolesToUser(Long userId, List<Long> roleIds) {
        // 实现角色分配逻辑
        // 这里需要调用UserRoleMapper
        System.out.println("为用户 " + userId + " 分配角色: " + roleIds);
        // TODO: 实现完整的角色分配逻辑
    }

    @Override
    public List<RoleDTO> getRolesByUserId(Long userId) {
        List<Role> roles = roleMapper.selectByUserId(userId);
        return BeanConvertUtil.convertList(roles, RoleDTO.class);
    }
}