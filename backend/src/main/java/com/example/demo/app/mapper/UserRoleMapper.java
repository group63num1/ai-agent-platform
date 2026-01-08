package com.example.demo.app.mapper;

import com.example.demo.app.entity.UserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface UserRoleMapper {

    int insert(UserRole userRole);

    int deleteByUserIdAndRoleId(@Param("userId") Long userId, @Param("roleId") Long roleId);

    int deleteByUserId(@Param("userId") Long userId);

    List<UserRole> selectByUserId(@Param("userId") Long userId);

    List<UserRole> selectByRoleId(@Param("roleId") Long roleId);

    UserRole selectByUserIdAndRoleId(@Param("userId") Long userId, @Param("roleId") Long roleId);
}
