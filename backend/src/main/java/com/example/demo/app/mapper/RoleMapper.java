// src/main/java/com/example/demo/app/mapper/RoleMapper.java
package com.example.demo.app.mapper;

import com.example.demo.app.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface RoleMapper {

    int insert(Role role);

    Role selectById(@Param("id") Long id);

    Role selectByCode(@Param("code") String code);

    List<Role> selectAll();

    // 添加这个缺失的方法
    List<Role> selectByUserId(@Param("userId") Long userId);

    List<Role> selectByStatus(@Param("status") Integer status);

    List<Role> selectSystemRoles();

    int update(Role role);

    int softDelete(@Param("id") Long id);
}