// src/main/java/com/example/demo/controller/UserController.java
package com.example.demo.controller;

import com.example.demo.common.ApiResponse;
import com.example.demo.dto.UserDTO;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 获取所有用户列表
     */
    @GetMapping
    public ApiResponse<List<UserDTO>> getAllUsers() {
        try {
            List<UserDTO> users = userService.getAllUsers();
            return ApiResponse.ok(users);
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    /**
     * 根据ID获取用户
     */
    @GetMapping("/{id}")
    public ApiResponse<UserDTO> getUserById(@PathVariable Long id) {
        try {
            UserDTO user = userService.getUserById(id);
            return ApiResponse.ok(user);
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    /**
     * 根据用户名获取用户
     */
    @GetMapping("/username/{username}")
    public ApiResponse<UserDTO> getUserByUsername(@PathVariable String username) {
        try {
            UserDTO user = userService.getUserByUsername(username);
            return ApiResponse.ok(user);
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/{id}")
    public ApiResponse<UserDTO> updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO) {
        try {
            UserDTO updatedUser = userService.updateUser(id, userDTO);
            return ApiResponse.ok(updatedUser);
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            // 使用明确的类型转换来避免泛型推断问题
            return new ApiResponse<>(0, "用户删除成功", null);
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }
}