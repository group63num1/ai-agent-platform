// src/main/java/com/example/demo/service/impl/PermissionServiceImpl.java
package com.example.demo.service.impl;

import com.example.demo.dto.PermissionDTO;
import com.example.demo.app.entity.Permission;
import com.example.demo.app.mapper.PermissionMapper;
import com.example.demo.service.PermissionService;
import com.example.demo.util.BeanConvertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PermissionServiceImpl implements PermissionService {

    @Autowired
    private PermissionMapper permissionMapper;

    @Override
    public List<PermissionDTO> getAllPermissions() {
        List<Permission> permissions = permissionMapper.selectAll();
        return BeanConvertUtil.convertList(permissions, PermissionDTO.class);
    }

    @Override
    public List<PermissionDTO> getPermissionsByModule(String module) {
        List<Permission> permissions = permissionMapper.selectByModule(module);
        return BeanConvertUtil.convertList(permissions, PermissionDTO.class);
    }

    @Override
    public List<String> getAllModules() {
        return permissionMapper.selectModules();
    }

    @Override
    public List<PermissionDTO> getPermissionsByUserId(Long userId) {
        List<Permission> permissions = permissionMapper.selectByUserId(userId);
        return BeanConvertUtil.convertList(permissions, PermissionDTO.class);
    }

    @Override
    public List<PermissionDTO> getPermissionsByRoleId(Long roleId) {
        List<Permission> permissions = permissionMapper.selectByRoleId(roleId);
        return BeanConvertUtil.convertList(permissions, PermissionDTO.class);
    }
}