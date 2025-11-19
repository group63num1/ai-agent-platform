// src/main/java/com/example/demo/service/UserService.java
package com.example.demo.service;

import com.example.demo.dto.UserCreateDTO;
import com.example.demo.dto.UserDTO;
import com.example.demo.dto.UserLoginDTO;

import java.util.List;

public interface UserService {

    // 用户注册
    UserDTO register(UserCreateDTO userCreateDTO);

    // 用户登录
    UserDTO login(UserLoginDTO userLoginDTO);

    // 根据ID获取用户
    UserDTO getUserById(Long id);

    // 根据用户名获取用户
    UserDTO getUserByUsername(String username);

    // 获取所有用户
    List<UserDTO> getAllUsers();

    // 更新用户信息
    UserDTO updateUser(Long id, UserDTO userDTO);

    // 更新头像
    UserDTO updateUserAvatar(Long id, String avatarUrl);

    // 删除用户（软删除）
    void deleteUser(Long id);

    // 硬删除用户（从数据库彻底删除）
    void hardDeleteUser(Long id);

    // 分配角色给用户
    void assignRolesToUser(Long userId, List<Long> roleIds);

    // 检查用户名是否存在
    boolean isUsernameExists(String username);

    // 检查邮箱是否存在
    boolean isEmailExists(String email);
}