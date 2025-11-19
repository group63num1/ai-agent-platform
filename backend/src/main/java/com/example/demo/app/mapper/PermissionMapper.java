// src/main/java/com/example/demo/app/mapper/PermissionMapper.java
package com.example.demo.app.mapper;

import com.example.demo.app.entity.Permission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface PermissionMapper {

    int insert(Permission permission);

    Permission selectById(@Param("id") Long id);

    Permission selectByCode(@Param("code") String code);

    List<Permission> selectAll();

    List<Permission> selectByModule(@Param("module") String module);

    List<Permission> selectByRoleId(@Param("roleId") Long roleId);

    List<Permission> selectByUserId(@Param("userId") Long userId);

    List<String> selectModules();

    int update(Permission permission);

    int softDelete(@Param("id") Long id);
}